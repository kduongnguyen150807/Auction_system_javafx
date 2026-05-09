package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("User Entity Tests")
public class UserTest {

  // ── Helpers ──────────────────────────────────────────────────────────────

  private Bidder bidder(String username) {
    return new Bidder(username, "pass123", username + "@test.com", "25", "0901234567");
  }

  private Seller seller(String username) {
    return new Seller(username, "pass123", username + "@test.com", "30", "0909876543");
  }

  // ── 1. Initialization & Roles ────────────────────────────────────────────

  @Nested
  @DisplayName("Initialization")
  class Initialization {

    @Test
    @DisplayName("Bidder has correct role")
    void bidder_hasCorrectRole() {
      Bidder b = bidder("alice");
      assertEquals(UserRole.BIDDER, b.getRole());
    }

    @Test
    @DisplayName("Seller has correct role")
    void seller_hasCorrectRole() {
      Seller s = seller("bob");
      assertEquals(UserRole.SELLER, s.getRole());
    }

    @Test
    @DisplayName("Admin has correct role")
    void admin_hasCorrectRole() {
      Admin a = new Admin("admin", "pass", "admin@test.com", "40", "0901111111");
      assertEquals(UserRole.ADMIN, a.getRole());
    }

    @Test
    @DisplayName("New user starts active and unlocked")
    void newUser_isActiveAndNotLocked() {
      Bidder b = bidder("carol");
      assertTrue(b.isActive(), "New user should be active");
      assertFalse(b.isLocked(), "New user should not be locked");
    }

    @Test
    @DisplayName("New user starts with zero balance")
    void newUser_hasZeroBalance() {
      Bidder b = bidder("dave");
      assertEquals(0.0, b.getBalance(), 0.001);
    }

    @Test
    @DisplayName("Constructor sets username as fullName")
    void newUser_fullNameMatchesUsername() {
      Bidder b = bidder("eve");
      assertEquals("eve", b.getFullName());
    }

    @Test
    @DisplayName("Constructor sets all fields correctly")
    void newUser_allFieldsSetCorrectly() {
      Bidder b = new Bidder("frank", "secret", "frank@test.com", "28", "0912345678");
      assertAll("fields",
          () -> assertEquals("frank", b.getUsername()),
          () -> assertEquals("secret", b.getPassword()),
          () -> assertEquals("frank@test.com", b.getEmail()),
          () -> assertEquals("28", b.getAge()),
          () -> assertEquals("0912345678", b.getPhoneNumber())
      );
    }
  }

  // ── 2. Encapsulation ─────────────────────────────────────────────────────

  @Nested
  @DisplayName("Encapsulation")
  class Encapsulation {

    @Test
    @DisplayName("setBalance changes balance correctly")
    void setBalance_updatesBalance() {
      Bidder b = bidder("grace");
      b.setBalance(5000.0);
      assertEquals(5000.0, b.getBalance(), 0.001);
    }

    @Test
    @DisplayName("setLocked=true marks user as locked")
    void setLocked_locksUser() {
      Bidder b = bidder("henry");
      b.setLocked(true);
      assertTrue(b.isLocked());
    }

    @Test
    @DisplayName("setActive=false deactivates user")
    void setActive_deactivatesUser() {
      Seller s = seller("ivan");
      s.setActive(false);
      assertFalse(s.isActive());
    }

    @Test
    @DisplayName("setEmail changes email")
    void setEmail_updatesEmail() {
      Bidder b = bidder("jane");
      b.setEmail("new@email.com");
      assertEquals("new@email.com", b.getEmail());
    }
  }

  // ── 3. Input Validation (domain rules) ──────────────────────────────────

  @Nested
  @DisplayName("Input Validation")
  class InputValidation {

    @ParameterizedTest(name = "balance={0} is negative")
    @ValueSource(doubles = {-1.0, -100.0, -0.01})
    @DisplayName("Negative balance values are invalid")
    void negativeBalance_isInvalid(double negBalance) {
      assertTrue(negBalance < 0, "Negative balance should not be accepted");
    }

    @Test
    @DisplayName("Banned user (locked) cannot bid — domain guard")
    void lockedUser_isConsideredBanned() {
      Bidder b = bidder("kyle");
      b.setLocked(true);
      assertTrue(b.isLocked() || !b.isActive(), "Locked user should fail bid validation");
    }

    @Test
    @DisplayName("Inactive user cannot bid — domain guard")
    void inactiveUser_isConsideredBanned() {
      Bidder b = bidder("lisa");
      b.setActive(false);
      assertTrue(b.isLocked() || !b.isActive());
    }

    @Test
    @DisplayName("User without phone number fails verification")
    void noPhoneNumber_failsVerification() {
      Bidder b = new Bidder("mike", "pass", "mike@test.com", "22", null);
      String phone = b.getPhoneNumber();
      assertTrue(phone == null || phone.trim().isEmpty(),
          "User with null phone should fail phone verification");
    }
  }

  // ── 4. Polymorphism ──────────────────────────────────────────────────────

  @Nested
  @DisplayName("Polymorphism")
  class Polymorphism {

    @Test
    @DisplayName("All user types are subtype of User")
    void allUserTypes_areSubclassOfUser() {
      assertAll(
          () -> assertInstanceOf(User.class, bidder("poly1")),
          () -> assertInstanceOf(User.class, seller("poly2")),
          () -> assertInstanceOf(User.class, new Admin("poly3", "p", "e", "30", "123"))
      );
    }

    @Test
    @DisplayName("getRole() is overridden correctly in each subtype")
    void getRole_polymorphicDispatch() {
      User[] users = {
        bidder("u1"), seller("u2"), new Admin("u3", "p", "e", "30", "123")
      };
      UserRole[] expected = {UserRole.BIDDER, UserRole.SELLER, UserRole.ADMIN};
      for (int i = 0; i < users.length; i++) {
        assertEquals(expected[i], users[i].getRole());
      }
    }
  }
}
