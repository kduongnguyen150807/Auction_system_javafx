package com.auction.server.service;

@Deprecated
public class AuctionCloser {
  private final SettlementService settlementService;

  public AuctionCloser() {
    this(new SettlementService());
  }

  public AuctionCloser(SettlementService settlementService) {
    this.settlementService = settlementService;
  }

  public void start() {
    settlementService.start();
  }

  public void stop() {
    settlementService.stop();
  }
}