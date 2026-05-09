package com.auction.server.utils;

import com.auction.shared.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

public class ClientValidator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientValidator.class);

  public static boolean validate(User user, List<ClientValidator.ValidationRule> validators) {
    for (ClientValidator.ValidationRule rule : validators) {
      if (rule.condition().test(user)) {
        LOGGER.info("Validation rule {} validated", rule.errorMessage());
        return false;
      }
    }
    return true;
  }

  public record ValidationRule(Predicate<User> condition, String errorMessage) {}
}
