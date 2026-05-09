package com.auction.client.ui.TrangChu;

import static org.junit.jupiter.api.Assertions.*;

import com.auction.shared.Art;
import com.auction.shared.AuctionType;
import com.auction.shared.Electronics;
import com.auction.shared.Item;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests catalog filter matching (keyword, price band, category lane, auction type). */
@DisplayName("AuctionFilterContext")
class AuctionFilterContextTest {

  private static Item electronics(String name, double price, AuctionType type) {
    Electronics it = new Electronics(name, "", price, price, 1);
    it.setAuctionType(type);
    return it;
  }

  private static Item artPiece(String name, double price, AuctionType type) {
    Art it = new Art(name, "", price, price, 1);
    it.setAuctionType(type);
    return it;
  }

  @Nested
  @DisplayName("matchesTrending")
  class Trending {

    @Test
    void allPassWhenFiltersWide() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "All", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      Item a = electronics("Watch", 100, AuctionType.ENGLISH);
      Item b = artPiece("Chair", 200, AuctionType.ENGLISH);
      assertTrue(ctx.matchesTrending(a));
      assertTrue(ctx.matchesTrending(b));
    }

    @Test
    void keywordCaseInsensitiveOnName() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("omega", "All", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      Item match = electronics("Omega Speed", 50, AuctionType.ENGLISH);
      Item miss = electronics("Rolex", 50, AuctionType.ENGLISH);
      assertTrue(ctx.matchesTrending(match));
      assertFalse(ctx.matchesTrending(miss));
    }

    @Test
    void categoryFilterExcludesOtherCategories() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "Electronics", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      Item el = electronics("Cable", 10, AuctionType.ENGLISH);
      Item art = artPiece("Painting", 10, AuctionType.ENGLISH);
      assertTrue(ctx.matchesTrending(el));
      assertFalse(ctx.matchesTrending(art));
    }

    @Test
    void priceBandInclusiveBounds() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "All", 100, 150, AuctionType.ENGLISH);
      Item low = electronics("Cheap", 99.99, AuctionType.ENGLISH);
      Item in = electronics("Mid", 125, AuctionType.ENGLISH);
      Item high = electronics("High", 150.01, AuctionType.ENGLISH);
      assertFalse(ctx.matchesTrending(low));
      assertTrue(ctx.matchesTrending(in));
      assertFalse(ctx.matchesTrending(high));
    }

    @Test
    void auctionTypeMustMatchCatalogFilter() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "All", 0, Double.MAX_VALUE, AuctionType.DUTCH);
      Item english = electronics("Lot", 10, AuctionType.ENGLISH);
      Item dutch = electronics("Lot2", 10, AuctionType.DUTCH);
      assertFalse(ctx.matchesTrending(english));
      assertTrue(ctx.matchesTrending(dutch));
    }
  }

  @Nested
  @DisplayName("matchesCategoryLane / itemsMatchingCategoryLane")
  class CategoryLanes {

    @Test
    void laneHidden_whenGlobalCategoryMismatch() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "Art", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      Item electronics = electronics("GPU", 100, AuctionType.ENGLISH);
      assertFalse(ctx.matchesCategoryLane(electronics, "Electronics"));
    }

    @Test
    void laneShowsItemsInLaneCategory_whenGlobalAll() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "All", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      List<Item> src =
          List.of(electronics("A", 10, AuctionType.ENGLISH), artPiece("B", 20, AuctionType.ENGLISH));
      List<Item> lane = ctx.itemsMatchingCategoryLane(src, "electronics");
      assertEquals(1, lane.size());
      assertEquals("A", lane.getFirst().getName());
    }

    @Test
    void nullItemCategoryTreatedAsEmptyCategoryString() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("", "All", 0, Double.MAX_VALUE, AuctionType.ENGLISH);
      Item uncategorized =
          new Electronics("U", "", 5, 5, 1) {
            @Override
            public String getCategory() {
              return null;
            }
          };
      uncategorized.setAuctionType(AuctionType.ENGLISH);
      assertTrue(ctx.matchesCategoryLane(uncategorized, ""));
    }
  }

  @Nested
  @DisplayName("itemsMatchingTrending")
  class ListOps {

    @Test
    void filtersList() {
      AuctionFilterContext ctx =
          new AuctionFilterContext("x", "All", 0, 1000, AuctionType.ENGLISH);
      List<Item> out =
          ctx.itemsMatchingTrending(
              List.of(
                  electronics("alpha", 5, AuctionType.ENGLISH),
                  electronics("beta x", 5, AuctionType.ENGLISH),
                  electronics("gamma x", 2000, AuctionType.ENGLISH)));
      assertEquals(1, out.size());
      assertEquals("beta x", out.getFirst().getName());
    }
  }
}
