package com.auction.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** TCP response after joining a live auction room. */
public class LiveSessionInfo implements Serializable {
  private static final long serialVersionUID = 1L;

  private int itemId;
  private int udpPort;
  private int participantCount;
  private List<LiveParticipantSummary> participants = new ArrayList<>();

  public LiveSessionInfo() {}

  public LiveSessionInfo(
      int itemId, int udpPort, int participantCount, List<LiveParticipantSummary> participants) {
    this.itemId = itemId;
    this.udpPort = udpPort;
    this.participantCount = participantCount;
    this.participants = participants != null ? participants : new ArrayList<>();
  }

  public int getItemId() {
    return itemId;
  }

  public void setItemId(int itemId) {
    this.itemId = itemId;
  }

  public int getUdpPort() {
    return udpPort;
  }

  public void setUdpPort(int udpPort) {
    this.udpPort = udpPort;
  }

  public int getParticipantCount() {
    return participantCount;
  }

  public void setParticipantCount(int participantCount) {
    this.participantCount = participantCount;
  }

  public List<LiveParticipantSummary> getParticipants() {
    return participants;
  }

  public void setParticipants(List<LiveParticipantSummary> participants) {
    this.participants = participants;
  }
}
