package com.auction.client.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("InputValidators")
class InputValidatorsTest {

  @ParameterizedTest(name = "valid email=''{0}''")
  @ValueSource(
      strings = {
        "u@example.com",
        "a.b@c.d",
        "name+tag@sub.domain.co",
        "foo_bar-1@localhost",
      })
  void acceptsValidEmails(String email) {
    assertTrue(InputValidators.isValidEmail(email));
  }

  @ParameterizedTest(name = "invalid=''{0}''")
  @ValueSource(
      strings = {
        "",
        "   ",
        "not-an-email",
        "@nodomain.com",
        "missing-at-sign.com",
        "space in@mail.com",
      })
  void rejectsInvalidOrBlankEmails(String email) {
    assertFalse(InputValidators.isValidEmail(email));
  }

  @Test
  @DisplayName("null email is rejected")
  void nullEmail() {
    assertFalse(InputValidators.isValidEmail(null));
  }

  @ParameterizedTest
  @NullSource
  @ValueSource(strings = {"", "   ", "\t"})
  @DisplayName("whitespace-only is rejected (trimmed empty)")
  void whitespaceOnlyRejected(String email) {
    assertFalse(InputValidators.isValidEmail(email));
  }

  @Test
  @DisplayName("leading/trailing spaces fail pattern (no implicit trim)")
  void noImplicitTrim() {
    assertFalse(InputValidators.isValidEmail(" user@example.com "));
    assertTrue(InputValidators.isValidEmail("user@example.com"));
  }
}
