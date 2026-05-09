package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.auction.shared.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrency Bidding Tests — Thread Safety")
public class ConcurrencyBiddingTest extends AbstractAuctionManagerMockingTest {

  private AuctionManager manager;
  private static final int THREAD_COUNT = 80;

  @BeforeEach
  void setUp() {
    manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
    bindAuctionManagerSingleton(manager);
  }

  private Item openItem(int id, double currentPrice) {
    Item item = ItemFactory.createItem("Electronics");
    item.setId(id);
    item.setSellerId(999);
    item.setStartingPrice(currentPrice);
    item.setCurrentPrice(currentPrice);
    item.setStatus(ItemStatus.OPEN);
    item.setEndTime(LocalDateTime.now().plusHours(2));
    return item;
  }

  private Bidder bidder(int id) {
    Bidder b = new Bidder("user" + id, "pass", "u" + id + "@x.com", "25", "09012345" + (id % 100));
    b.setId(id);
    b.setActive(true);
    b.setLocked(false);
    b.setBalance(1_000_000.0);
    return b;
  }

  @Test
  @DisplayName("Different auction locks are independent — no cross-blocking")
  void differentAuctions_haveSeparateLocks() {
    ReentrantLock lockA = manager.getAuctionLock(1L);
    ReentrantLock lockB = manager.getAuctionLock(2L);
    ReentrantLock lockA2 = manager.getAuctionLock(1L);

    assertNotSame(lockA, lockB, "Different item IDs must have different locks");
    assertSame(lockA, lockA2, "Same item ID must always return the same lock");
  }

  @Test
  @DisplayName("Two auctions proceed in parallel without deadlock")
  void twoAuctions_proceedInParallel() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(2);
    AtomicInteger doneCount = new AtomicInteger(0);

    Runnable lockItem1 = () -> {
      ReentrantLock l = manager.getAuctionLock(10L);
      l.lock();
      try {
        Thread.sleep(20);
        doneCount.incrementAndGet();
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      } finally {
        l.unlock();
        latch.countDown();
      }
    };

    Runnable lockItem2 = () -> {
      ReentrantLock l = manager.getAuctionLock(20L);
      l.lock();
      try {
        Thread.sleep(20);
        doneCount.incrementAndGet();
      } catch (InterruptedException ignored) {
        Thread.currentThread().interrupt();
      } finally {
        l.unlock();
        latch.countDown();
      }
    };

    long start = System.currentTimeMillis();
    new Thread(lockItem1).start();
    new Thread(lockItem2).start();
    assertTrue(latch.await(500, TimeUnit.MILLISECONDS), "Both auctions should finish in parallel within 500 ms");
    long elapsed = System.currentTimeMillis() - start;
    assertEquals(2, doneCount.get());
    assertTrue(elapsed < 200, "Parallel auctions should complete in ~20ms (parallel), not ~40ms (serial). Elapsed: " + elapsed + "ms");
  }

  @Test
  @DisplayName("80 concurrent bidders: only one succeeds via transactional deductBalanceTx")
  void concurrentBids_onlyOneSucceeds() throws InterruptedException, SQLException {
    Item item = openItem(100, 500.0);
    AtomicInteger deductCallCount = new AtomicInteger(0);

    for (int i = 1; i <= THREAD_COUNT; i++) {
      int uid = i;
      when(userDao.getById(String.valueOf(uid))).thenReturn(bidder(uid));
    }
    when(itemDao.getById(100)).thenReturn(item);

    when(userDao.deductBalanceTx(anyInt(), eq(600.0), eq(jdbcConn))).thenAnswer(inv -> deductCallCount.incrementAndGet() == 1);

    when(logDao.insertLogTx(anyInt(), eq("BID_HOLD"), eq(-600.0), eq(100), eq(jdbcConn))).thenReturn(true);
    when(bidDao.getCurrentHighestBidderTx(eq(100), eq(jdbcConn))).thenReturn(-1);
    when(bidDao.placeBidTx(any(BidTransaction.class), eq(jdbcConn))).thenReturn(true);
    when(itemDao.updatePriceTx(eq(100), eq(600.0), eq(jdbcConn))).thenReturn(true);

    ExecutorService pool = Executors.newFixedThreadPool(THREAD_COUNT);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);
    List<Response> responses = new CopyOnWriteArrayList<>();

    for (int i = 1; i <= THREAD_COUNT; i++) {
      int uid = i;
      pool.submit(() -> {
        try {
          startGate.await();
          BidTransaction bid = new BidTransaction(100, uid, 600.0);
          responses.add(manager.processBid(bid));
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          endGate.countDown();
        }
      });
    }

    startGate.countDown();
    assertTrue(endGate.await(10, TimeUnit.SECONDS), "All threads should finish within 10 seconds");
    pool.shutdown();

    long okCount = responses.stream().filter(r -> Response.OK.equals(r.getStatus())).count();
    assertEquals(1, okCount, "Exactly ONE bid should succeed when only first deduction is accepted");
    assertEquals(THREAD_COUNT, responses.size(), "Every thread must receive a response (no lost responses)");
  }

  @Test
  @DisplayName("tryLock: threads waiting > 500ms receive 'busy' response")
  void tryLock_preventsInfiniteBlocking() throws InterruptedException {
    ReentrantLock lock = manager.getAuctionLock(200L);
    lock.lock();

    Bidder bidder = bidder(1);
    when(userDao.getById("1")).thenReturn(bidder);

    CountDownLatch done = new CountDownLatch(1);
    AtomicInteger busyCount = new AtomicInteger(0);

    Thread t = new Thread(() -> {
      BidTransaction bid = new BidTransaction(200, 1, 700.0);
      Response r = manager.processBid(bid);
      if (Response.ERROR.equals(r.getStatus()) && r.getMessage().toLowerCase().contains("busy")) {
        busyCount.incrementAndGet();
      }
      done.countDown();
    });
    t.start();

    assertTrue(done.await(2, TimeUnit.SECONDS), "Thread should unblock after tryLock timeout");
    assertEquals(1, busyCount.get(), "Thread should receive 'busy' response after timeout");
    lock.unlock();
    t.join();
  }

  @Test
  @DisplayName("Concurrent lock acquisitions on same auction are serialized — no race")
  void concurrentAccess_sameAuction_serialized() throws InterruptedException {
    ReentrantLock lock = manager.getAuctionLock(300L);
    AtomicInteger concurrentCount = new AtomicInteger(0);
    AtomicInteger maxConcurrent = new AtomicInteger(0);
    CountDownLatch startGate = new CountDownLatch(1);
    CountDownLatch endGate = new CountDownLatch(50);

    ExecutorService pool = Executors.newFixedThreadPool(50);
    for (int i = 0; i < 50; i++) {
      pool.submit(() -> {
        try {
          startGate.await();
          lock.lock();
          try {
            int cur = concurrentCount.incrementAndGet();
            maxConcurrent.updateAndGet(prev -> Math.max(prev, cur));
            Thread.sleep(1);
            concurrentCount.decrementAndGet();
          } finally {
            lock.unlock();
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          endGate.countDown();
        }
      });
    }

    startGate.countDown();
    assertTrue(endGate.await(10, TimeUnit.SECONDS));
    pool.shutdown();

    assertEquals(1, maxConcurrent.get(),
        "Maximum concurrent threads inside the lock should always be 1 — no data race");
  }
}
