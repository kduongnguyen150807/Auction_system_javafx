package com.auction.client;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ClientValidationTest verifies all UI input-validation logic used before
 * network requests are dispatched from the client. All helpers tested here
 * are pure functions — no JavaFX toolkit, no network, no filesystem.
 */
@DisplayName("Client-Side Input Validation Tests")
public class ClientValidationTest {

  // ═══════════════════════════════════════════════════════════════════════════
  //  Inline validation helpers (mirrors logic from controllers)
  // ═══════════════════════════════════════════════════════════════════════════

  /** Parses and validates a bid amount string entered by the user. */
  private static double parseBidAmount(String raw) {
    if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Bid amount is empty");
    String cleaned = raw.replace("$", "").replace(",", "").trim();
    double value;
    try {
      value = Double.parseDouble(cleaned);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid number: " + raw);
    }
    if (value <= 0) throw new IllegalArgumentException("Bid must be positive");
    return value;
  }

  /** Validates an IP address string (simple format check). */
  private static boolean isValidIp(String ip) {
    if (ip == null || ip.isBlank()) return false;
    String[] parts = ip.split("\\.");
    if (parts.length != 4) return false;
    for (String part : parts) {
      try {
        int val = Integer.parseInt(part);
        if (val < 0 || val > 255) return false;
      } catch (NumberFormatException e) {
        return false;
      }
    }
    return true;
  }

  /** Validates a username: non-null, 3-20 chars, alphanumeric + underscore. */
  private static boolean isValidUsername(String username) {
    return username != null && username.matches("[a-zA-Z0-9_]{3,20}");
  }

  /** Validates a password: at least 6 characters. */
  private static boolean isValidPassword(String password) {
    return password != null && password.length() >= 6;
  }

  /** Validates email format. */
  private static boolean isValidEmail(String email) {
    return email != null && email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
  }

  /** Validates phone number: exactly 10 digits, starts with 0. */
  private static boolean isValidPhoneNumber(String phone) {
    return phone != null && phone.matches("0[0-9]{9}");
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 1. Bid Amount Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Bid Amount Validation")
  class BidAmountValidation {

    @Test
    @DisplayName("Valid integer amount is parsed correctly")
    void validInteger_parsedCorrectly() {
      assertEquals(1000.0, parseBidAmount("1000"), 0.001);
    }

    @Test
    @DisplayName("Valid decimal amount is parsed correctly")
    void validDecimal_parsedCorrectly() {
      assertEquals(1500.50, parseBidAmount("1500.50"), 0.001);
    }

    @Test
    @DisplayName("Dollar sign and commas are stripped before parsing")
    void stripsFormattingChars() {
      assertEquals(10000.0, parseBidAmount("$10,000"), 0.001);
      assertEquals(1500.0, parseBidAmount("1,500"), 0.001);
    }

    @Test
    @DisplayName("Whitespace is trimmed before parsing")
    void trimmedWhitespace_parsedCorrectly() {
      assertEquals(250.0, parseBidAmount("  250  "), 0.001);
    }

    @ParameterizedTest(name = "invalid input=''{0}''")
    @ValueSource(strings = {"abc", "12.34.56", "1e5x", "!@#", "12abc"})
    @DisplayName("Non-numeric strings throw IllegalArgumentException")
    void nonNumericInput_throwsException(String input) {
      assertThrows(IllegalArgumentException.class, () -> parseBidAmount(input));
    }

    @ParameterizedTest(name = "non-positive value={0}")
    @ValueSource(doubles = {0.0, -1.0, -0.01, -999.0})
    @DisplayName("Non-positive bid values throw IllegalArgumentException")
    void nonPositiveBid_throwsException(double amount) {
      assertThrows(IllegalArgumentException.class, () -> parseBidAmount(String.valueOf(amount)));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Null or blank input throws IllegalArgumentException")
    void nullOrBlankInput_throwsException(String input) {
      assertThrows(IllegalArgumentException.class, () -> parseBidAmount(input));
    }

    @Test
    @DisplayName("Very large bid amount is accepted")
    void veryLargeAmount_accepted() {
      assertDoesNotThrow(() -> parseBidAmount("999999999"));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 2. IP Address Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Server IP Address Validation")
  class IpValidation {

    @ParameterizedTest(name = "valid IP=''{0}''")
    @ValueSource(strings = {"127.0.0.1", "192.168.1.100", "10.0.0.1", "255.255.255.255", "0.0.0.0"})
    @DisplayName("Valid IPv4 addresses are accepted")
    void validIp_isAccepted(String ip) {
      assertTrue(isValidIp(ip));
    }

    @ParameterizedTest(name = "invalid IP=''{0}''")
    @ValueSource(strings = {"256.0.0.1", "192.168.1", "abc.def.ghi.jkl", "192.168.1.1.1", "192.168..1"})
    @DisplayName("Invalid IPv4 addresses are rejected")
    void invalidIp_isRejected(String ip) {
      assertFalse(isValidIp(ip));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    @DisplayName("Null or blank IP is rejected")
    void nullOrBlankIp_isRejected(String ip) {
      assertFalse(isValidIp(ip));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 3. Username Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Username Validation")
  class UsernameValidation {

    @ParameterizedTest(name = "valid username=''{0}''")
    @ValueSource(strings = {"alice", "Bob123", "user_name", "abc", "A1B2C3D4E5F6G7H8I9J0"})
    @DisplayName("Valid usernames are accepted")
    void validUsername_isAccepted(String username) {
      assertTrue(isValidUsername(username));
    }

    @ParameterizedTest(name = "invalid username=''{0}''")
    @ValueSource(strings = {"ab", "this_is_a_very_long_username_over_20", "user name", "user@name", "user-name"})
    @DisplayName("Invalid usernames are rejected")
    void invalidUsername_isRejected(String username) {
      assertFalse(isValidUsername(username));
    }

    @Test
    @DisplayName("Null username is rejected")
    void nullUsername_isRejected() {
      assertFalse(isValidUsername(null));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 4. Password Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Password Validation")
  class PasswordValidation {

    @ParameterizedTest(name = "valid password length={0}")
    @ValueSource(strings = {"123456", "abcdef", "password123", "P@ssw0rd!"})
    @DisplayName("Passwords with >= 6 chars are accepted")
    void validPassword_isAccepted(String password) {
      assertTrue(isValidPassword(password));
    }

    @ParameterizedTest(name = "too short=''{0}''")
    @ValueSource(strings = {"12345", "abc", "", "x"})
    @DisplayName("Passwords shorter than 6 chars are rejected")
    void shortPassword_isRejected(String password) {
      assertFalse(isValidPassword(password));
    }

    @Test
    @DisplayName("Null password is rejected")
    void nullPassword_isRejected() {
      assertFalse(isValidPassword(null));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 5. Email Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Email Validation")
  class EmailValidation {

    @ParameterizedTest(name = "valid=''{0}''")
    @ValueSource(strings = {"user@example.com", "alice.bob@mail.org", "test+tag@domain.io"})
    @DisplayName("Valid email formats are accepted")
    void validEmail_isAccepted(String email) {
      assertTrue(isValidEmail(email));
    }

    @ParameterizedTest(name = "invalid=''{0}''")
    @ValueSource(strings = {"notanemail", "@nodomain.com", "missing@", "no space @test.com"})
    @DisplayName("Invalid email formats are rejected")
    void invalidEmail_isRejected(String email) {
      assertFalse(isValidEmail(email));
    }

    @Test
    @DisplayName("Null email is rejected")
    void nullEmail_isRejected() {
      assertFalse(isValidEmail(null));
    }
  }

  // ═══════════════════════════════════════════════════════════════════════════
  // 6. Phone Number Validation
  // ═══════════════════════════════════════════════════════════════════════════

  @Nested
  @DisplayName("Phone Number Validation")
  class PhoneValidation {

    @ParameterizedTest(name = "valid=''{0}''")
    @ValueSource(strings = {"0901234567", "0123456789", "0999999999"})
    @DisplayName("Valid Vietnamese phone numbers are accepted")
    void validPhone_isAccepted(String phone) {
      assertTrue(isValidPhoneNumber(phone));
    }

    @ParameterizedTest(name = "invalid=''{0}''")
    @ValueSource(strings = {"123456789", "090123456", "09012345678", "0abc123456", "1901234567"})
    @DisplayName("Invalid phone numbers are rejected")
    void invalidPhone_isRejected(String phone) {
      assertFalse(isValidPhoneNumber(phone));
    }

    @Test
    @DisplayName("Null phone number is rejected")
    void nullPhone_isRejected() {
      assertFalse(isValidPhoneNumber(null));
    }
  }
}
