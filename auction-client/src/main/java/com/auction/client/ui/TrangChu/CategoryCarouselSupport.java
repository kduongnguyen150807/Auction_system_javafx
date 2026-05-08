package com.auction.client.ui.TrangChu;

import com.auction.shared.Item;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

/** Carousel window math + lane constants (category slots on the home catalog). */
public final class CategoryCarouselSupport {

  private CategoryCarouselSupport() {}

  /** Visible cards per category shelf when using carousel arrows. */
  public static final int MAX_SLOTS_PER_CATEGORY = 3;

  /** Shelf order on the home page; must stay aligned with search category values. */
  public static final String[] SLOT_CATEGORIES = {"Art", "Electronics", "Vehicle"};

  public static int clampOffset(int offset, int totalItems, int maxSlots) {
    if (totalItems <= maxSlots) {
      return 0;
    }
    int maxOffset = totalItems - maxSlots;
    return Math.max(0, Math.min(offset, maxOffset));
  }

  public static List<Item> sliceWindow(List<Item> all, int fromIndex, int maxSlots) {
    int n = all.size();
    if (n <= maxSlots) {
      return new ArrayList<>(all);
    }
    int from = clampOffset(fromIndex, n, maxSlots);
    int to = from + maxSlots;
    return new ArrayList<>(all.subList(from, to));
  }

  public static int laneIndexFromAction(
      ActionEvent event, Button[] prevButtons, Button[] nextButtons) {
    Object src = event.getSource();
    if (prevButtons != null) {
      for (int i = 0; i < prevButtons.length; i++) {
        if (prevButtons[i] == src) {
          return i;
        }
      }
    }
    if (nextButtons != null) {
      for (int i = 0; i < nextButtons.length; i++) {
        if (nextButtons[i] == src) {
          return i;
        }
      }
    }
    return 0;
  }

  public static void updateNavButtons(
      int totalItems,
      int offset,
      int maxSlots,
      Button prev,
      Button next) {
    boolean showArrows = totalItems > maxSlots;
    if (prev != null) {
      prev.setVisible(showArrows);
      prev.setManaged(showArrows);
    }
    if (next != null) {
      next.setVisible(showArrows);
      next.setManaged(showArrows);
    }
    if (!showArrows || prev == null || next == null) {
      return;
    }
    int maxOff = totalItems - maxSlots;
    prev.setDisable(offset <= 0);
    next.setDisable(offset >= maxOff);
  }
}
