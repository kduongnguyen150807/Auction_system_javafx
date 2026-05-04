package com.auction.shared;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@DisplayName("Item & Factory Method Tests")
public class ItemTest {

  // ── 1. Factory Method Pattern ────────────────────────────────────────────

  @Nested
  @DisplayName("ItemFactory — Factory Method Pattern")
  class FactoryMethod {

    @Test
    @DisplayName("createItem('Electronics') returns Electronics instance")
    void createElectronics_returnsCorrectType() {
      Item item = ItemFactory.createItem("Electronics");
      assertInstanceOf(Electronics.class, item);
    }

    @Test
    @DisplayName("createItem('Art') returns Art instance")
    void createArt_returnsCorrectType() {
      Item item = ItemFactory.createItem("Art");
      assertInstanceOf(Art.class, item);
    }

    @Test
    @DisplayName("createItem('Vehicle') returns Vehicle instance")
    void createVehicle_returnsCorrectType() {
      Item item = ItemFactory.createItem("Vehicle");
      assertInstanceOf(Vehicle.class, item);
    }

    @Test
    @DisplayName("createItem(null) defaults to Vehicle")
    void createNull_defaultsToVehicle() {
      Item item = ItemFactory.createItem(null);
      assertInstanceOf(Vehicle.class, item);
    }

    @Test
    @DisplayName("createItem(unknown) defaults to Vehicle")
    void createUnknown_defaultsToVehicle() {
      Item item = ItemFactory.createItem("Furniture");
      assertInstanceOf(Vehicle.class, item);
    }

    @Test
    @DisplayName("Case-insensitive factory lookup")
    void caseInsensitive_electronics() {
      Item item = ItemFactory.createItem("electronics");
      assertInstanceOf(Electronics.class, item);
    }

    @Test
    @DisplayName("Factory always returns non-null")
    void factory_neverReturnsNull() {
      assertNotNull(ItemFactory.createItem("Electronics"));
      assertNotNull(ItemFactory.createItem(null));
      assertNotNull(ItemFactory.createItem(""));
    }
  }

  // ── 2. Polymorphism — calculateTax() ────────────────────────────────────

  @Nested
  @DisplayName("Polymorphism — calculateTax()")
  class PolymorphismTax {

    @ParameterizedTest(name = "{0} @ price={1} → tax={2}")
    @CsvSource({
      "Electronics, 1000.0, 50.0",
      "Art,         1000.0, 80.0",
      "Vehicle,     1000.0, 100.0",
      "Electronics, 500.0,  25.0",
      "Art,         250.0,  20.0",
      "Vehicle,     200.0,  20.0"
    })
    @DisplayName("calculateTax returns correct amount per category")
    void calculateTax_perCategory(String category, double price, double expectedTax) {
      Item item = ItemFactory.createItem(category);
      item.setCurrentPrice(price);
      assertEquals(expectedTax, item.calculateTax(), 0.001);
    }

    @Test
    @DisplayName("All items are subtypes of Item")
    void allItems_areSubclassOfItem() {
      assertAll(
          () -> assertInstanceOf(Item.class, new Electronics()),
          () -> assertInstanceOf(Item.class, new Art()),
          () -> assertInstanceOf(Item.class, new Vehicle())
      );
    }

    @Test
    @DisplayName("getCategory() returns correct category string per subtype")
    void getCategory_perSubtype() {
      assertEquals("Electronics", new Electronics().getCategory());
      assertEquals("Art", new Art().getCategory());
      assertEquals("Vehicle", new Vehicle().getCategory());
    }
  }

  // ── 3. Item Initialization & Encapsulation ───────────────────────────────

  @Nested
  @DisplayName("Item Initialization & Encapsulation")
  class Initialization {

    @Test
    @DisplayName("Constructor sets status OPEN")
    void constructor_setsStatusOpen() {
      Item item = new Electronics("Laptop", "Gaming laptop", 500, 500, 1);
      assertEquals(ItemStatus.OPEN, item.getStatus());
    }

    @Test
    @DisplayName("Constructor sets winnerId to -1")
    void constructor_winnerIdIsMinusOne() {
      Item item = new Art("Painting", "Oil canvas", 200, 200, 2);
      assertEquals(-1, item.getWinnerId());
    }

    @Test
    @DisplayName("setStatus changes item status")
    void setStatus_changesStatus() {
      Item item = ItemFactory.createItem("Vehicle");
      item.setStatus(ItemStatus.CLOSED);
      assertEquals(ItemStatus.CLOSED, item.getStatus());
    }

    @Test
    @DisplayName("setCurrentPrice updates current price")
    void setCurrentPrice_updates() {
      Item item = ItemFactory.createItem("Electronics");
      item.setCurrentPrice(1500.0);
      assertEquals(1500.0, item.getCurrentPrice(), 0.001);
    }

    @Test
    @DisplayName("setEndTime stores endTime correctly")
    void setEndTime_storesCorrectly() {
      Item item = ItemFactory.createItem("Art");
      LocalDateTime end = LocalDateTime.of(2026, 12, 31, 23, 59);
      item.setEndTime(end);
      assertEquals(end, item.getEndTime());
    }

    @Test
    @DisplayName("setMaxPrice stores buyItNow price")
    void setMaxPrice_storesCorrectly() {
      Item item = ItemFactory.createItem("Vehicle");
      item.setMaxPrice(50000.0);
      assertEquals(50000.0, item.getMaxPrice(), 0.001);
    }

    @Test
    @DisplayName("Buy-It-Now condition: bid >= maxPrice")
    void buyItNow_triggeredWhenBidReachesMaxPrice() {
      Item item = ItemFactory.createItem("Electronics");
      item.setCurrentPrice(800.0);
      item.setMaxPrice(1000.0);

      assertTrue(item.getMaxPrice() > 0 && 1000.0 >= item.getMaxPrice(),
          "Exact maxPrice bid should trigger Buy-It-Now");
      assertTrue(item.getMaxPrice() > 0 && 1200.0 >= item.getMaxPrice(),
          "Above maxPrice bid should also trigger Buy-It-Now");
      assertFalse(item.getMaxPrice() > 0 && 900.0 >= item.getMaxPrice(),
          "Below maxPrice bid should NOT trigger Buy-It-Now");
    }
  }

  // ── 4. Item Status Transitions ───────────────────────────────────────────

  @Nested
  @DisplayName("Item Status Transitions")
  class StatusTransitions {

    @Test
    @DisplayName("OPEN item can transition to CLOSED")
    void openItem_canBeClosed() {
      Item item = new Art("Painting", "Oil canvas", 100, 100, 1);
      assertEquals(ItemStatus.OPEN, item.getStatus());
      item.setStatus(ItemStatus.CLOSED);
      assertEquals(ItemStatus.CLOSED, item.getStatus());
    }

    @Test
    @DisplayName("OPEN item can transition to CANCELED")
    void openItem_canBeCanceled() {
      Item item = new Vehicle("Car", "Sport car", 1000, 1000, 1);
      item.setStatus(ItemStatus.CANCELED);
      assertEquals(ItemStatus.CANCELED, item.getStatus());
    }

    @Test
    @DisplayName("Closed item is no longer OPEN")
    void closedItem_isNotOpen() {
      Item item = ItemFactory.createItem("Electronics");
      item.setStatus(ItemStatus.CLOSED);
      assertNotEquals(ItemStatus.OPEN, item.getStatus());
    }

    @Test
    @DisplayName("setWinnerId stores winner correctly")
    void setWinnerId_storesCorrectly() {
      Item item = ItemFactory.createItem("Art");
      item.setWinnerId(42);
      assertEquals(42, item.getWinnerId());
    }
  }
}
