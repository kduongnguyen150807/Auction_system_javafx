package com.auction.server.service.spin;

import com.auction.server.dao.user.UserDao;
import com.auction.shared.SpinWheelResult;
import com.auction.shared.SpinWheelSegment;
import com.auction.shared.SpinWheelState;
import com.auction.shared.User;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

public final class SpinWheelService {
  private static final SpinWheelService INSTANCE = new SpinWheelService();
  private final Random random = new Random();

  private SpinWheelService() {}

  public static SpinWheelService getInstance() {
    return INSTANCE;
  }

  public SpinWheelState buildState(User user) {
    SpinWheelState state = new SpinWheelState();
    if (user == null) {
      return state;
    }
    boolean free = isFreeSpinAvailable(user);
    state.setFreeSpinAvailable(free);
    state.setNextFreeSpinAt(free ? null : nextMidnightAfterToday());
    state.setPaidSpinCredits(user.getPaidSpinCredits());
    state.setSpinCreditPrice(SpinWheelState.SPIN_CREDIT_PRICE);
    state.setSegments(SpinWheelSegment.allSegments());
    return state;
  }

  public SpinWheelResult spin(User user, UserDao userDao) {
    SpinWheelResult result = new SpinWheelResult();
    if (user == null) {
      result.setMessage("Unauthorized");
      return result;
    }

    boolean useFree = isFreeSpinAvailable(user);
    if (!useFree && user.getPaidSpinCredits() <= 0) {
      result.setMessage("Bạn đã hết lượt quay. Mua thêm hoặc chờ lượt miễn phí ngày mai.");
      fillState(result, user);
      return result;
    }

    if (useFree) {
      if (!userDao.markDailySpinUsed(user.getId(), LocalDateTime.now())) {
        result.setMessage("Không thể ghi nhận lượt quay miễn phí.");
        fillState(result, user);
        return result;
      }
    } else if (!userDao.decrementPaidSpinCredits(user.getId())) {
      result.setMessage("Không đủ lượt quay đã mua.");
      fillState(result, user);
      return result;
    }

    SpinWheelSegment prize = SpinWheelSegment.roll(random);
    applyPrize(user.getId(), prize, userDao);

    User fresh = userDao.getById(String.valueOf(user.getId()));
    if (fresh != null) {
      fresh.setPassword("");
    }

    result.setSegmentIndex(prize.getIndex());
    result.setPrizeLabel(prize.getLabel());
    result.setMessage(buildWinMessage(prize));
    result.setUser(fresh);
    if (fresh != null) {
      fillState(result, fresh);
    }
    return result;
  }

  public User buyCredits(User user, int count, UserDao userDao) {
    if (user == null || count <= 0) {
      return null;
    }
    double total = count * SpinWheelState.SPIN_CREDIT_PRICE;
    if (!userDao.purchaseSpinCredits(user.getId(), count, total)) {
      return null;
    }
    User fresh = userDao.getById(String.valueOf(user.getId()));
    if (fresh != null) {
      fresh.setPassword("");
    }
    return fresh;
  }

  static boolean isFreeSpinAvailable(User user) {
    if (user.getLastDailySpinAt() == null) {
      return true;
    }
    LocalDate last = user.getLastDailySpinAt().toLocalDate();
    return last.isBefore(LocalDate.now());
  }

  static LocalDateTime nextMidnightAfterToday() {
    return LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT);
  }

  private void applyPrize(int userId, SpinWheelSegment prize, UserDao userDao) {
    switch (prize) {
      case VIP_1_DAY -> userDao.extendVipDays(userId, 1);
      case VIP_2_DAYS -> userDao.extendVipDays(userId, 2);
      case VIP_1_YEAR -> userDao.extendVipDays(userId, 365);
      case CASH_10 -> userDao.atomicCreditBalance(userId, 10);
      case CASH_100 -> userDao.atomicCreditBalance(userId, 100);
      case CASH_10000 -> userDao.atomicCreditBalance(userId, 10_000);
      case BETTER_LUCK -> {}
      default -> {}
    }
  }

  private static String buildWinMessage(SpinWheelSegment prize) {
    return switch (prize) {
      case BETTER_LUCK -> "Chúc bạn may mắn lần sau!";
      case VIP_1_DAY, VIP_2_DAYS, VIP_1_YEAR -> "Chúc mừng! Bạn nhận được " + prize.getLabel() + "!";
      default -> "Chúc mừng! Bạn trúng " + prize.getLabel() + "!";
    };
  }

  private static void fillState(SpinWheelResult result, User user) {
    boolean free = isFreeSpinAvailable(user);
    result.setFreeSpinAvailable(free);
    result.setNextFreeSpinAt(free ? null : nextMidnightAfterToday());
    result.setPaidSpinCredits(user.getPaidSpinCredits());
  }
}
