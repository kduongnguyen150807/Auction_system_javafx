package com.auction.shared.linkv2;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Request<T> implements Serializable {

  private final RequestType type;
  private final String id;
  private final T data;

  private LocalDateTime timestamp;

  public Request(RequestType type, T data) {
    this.id = UUID.randomUUID().toString();

    this.type = type;
    this.data = data;
  }

  public static <T> Request<T> of(RequestType type, T data) {
    return new Request<>(type, data);
  }

  public String getId() {
    return id;
  }

  public RequestType getType() {
    return type;
  }

  public T getData() {
    return data;
  }

  @Override
  public String toString() {
    return "Request{" +
      "action='" + type.toString() + '\'' +
      ", payload=" + data +
      '}';
  }

}
