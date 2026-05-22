# Live Auction — Design Spec

**Date:** 2026-05-22  
**Status:** Approved

## Summary

New auction type `LIVE` with UDP server-relay video, participant video grid, and combined quick-tier + custom bid UI. Follows Open-Closed Principle via strategy/registry/handler patterns.

## Architecture

- **TCP :8080** — session join/leave, bid tiers, bidding, push events
- **UDP :9090** — JPEG frame relay between live session participants
- **BiddingStrategyRegistry** — pluggable strategies per `AuctionType`
- **VideoTransport** (client) — swappable UDP implementation

## Key Components

| Layer | Components |
|-------|------------|
| Shared | `AuctionType.LIVE`, `LiveSessionInfo`, `LiveBidTiers`, `LiveVideoPacket` |
| Server | `VideoRelayServer`, `LiveSessionManager`, `LiveBiddingStrategy`, live handlers |
| Client | `LiveAuctionController`, `UdpRelayVideoTransport`, sidebar menu |

## Bid Rules (LIVE)

Same as English ascending auction; must join live session; no auto-bid; anti-snipe +60s.

## UDP Packet

`[itemId:4][userId:4][timestamp:8][jpeg...]`
