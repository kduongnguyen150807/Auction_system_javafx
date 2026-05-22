package com.auction.server.service.auction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.auction.server.live.LiveSessionManager;
import com.auction.shared.Bidder;
import com.auction.shared.LiveSessionInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LiveSessionManager")
class LiveSessionManagerTest extends AbstractAuctionManagerMockingTest {

  private LiveSessionManager sessions;

  @BeforeEach
  void setUpSessions() {
    LiveSessionManager.resetForTest();
    sessions = LiveSessionManager.getInstance();
    AuctionManager manager = new AuctionManager(itemDao, userDao, bidDao, logDao);
    bindAuctionManagerSingleton(manager);
  }

  @AfterEach
  void tearDownSessions() {
    LiveSessionManager.resetForTest();
  }

  private Bidder bidder(int id, String name) {
    Bidder b = new Bidder(name, "pass", name + "@test.com", "25", "090000000" + id);
    b.setId(id);
    b.setActive(true);
    return b;
  }

  @Test
  void join_addsParticipant() {
    Bidder alice = bidder(1, "alice");
    when(userDao.getById("1")).thenReturn(alice);

    LiveSessionInfo info = sessions.join(alice, 42, 9090);

    assertEquals(42, info.getItemId());
    assertEquals(9090, info.getUdpPort());
    assertEquals(1, info.getParticipantCount());
    assertTrue(sessions.isParticipant(42, 1));
  }

  @Test
  void join_sameUserDifferentRoom_movesParticipant() {
    Bidder alice = bidder(1, "alice");
    when(userDao.getById("1")).thenReturn(alice);

    sessions.join(alice, 10, 9090);
    sessions.join(alice, 20, 9090);

    assertFalse(sessions.isParticipant(10, 1));
    assertTrue(sessions.isParticipant(20, 1));
  }

  @Test
  void leave_removesParticipant() {
    Bidder alice = bidder(1, "alice");
    Bidder bob = bidder(2, "bob");
    when(userDao.getById("1")).thenReturn(alice);
    when(userDao.getById("2")).thenReturn(bob);

    sessions.join(alice, 5, 9090);
    sessions.join(bob, 5, 9090);
    sessions.leave(1);

    assertFalse(sessions.isParticipant(5, 1));
    assertTrue(sessions.isParticipant(5, 2));
  }

  @Test
  void buildSessionInfo_listsAllParticipants() {
    Bidder alice = bidder(1, "alice");
    Bidder bob = bidder(2, "bob");
    when(userDao.getById("1")).thenReturn(alice);
    when(userDao.getById("2")).thenReturn(bob);

    sessions.join(alice, 7, 9090);
    sessions.join(bob, 7, 9090);

    LiveSessionInfo info = sessions.buildSessionInfo(7, 9090);
    assertEquals(2, info.getParticipantCount());
    assertEquals(2, info.getParticipants().size());
  }
}
