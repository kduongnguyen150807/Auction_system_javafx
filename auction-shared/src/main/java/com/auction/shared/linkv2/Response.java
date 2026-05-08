package com.auction.shared.linkv2;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Response<T> implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;
  private final ResponseStatus status;
  private final String id;
  private final String message;
  private final T data;
  protected LocalDateTime timestamp;

  private Response(String id, ResponseStatus status, String message, T data) {
    this.id = id;
    this.status = status;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  public static <T> Response<T> success(String id, String message, T data) {
    return new Response<>(id, ResponseStatus.SUCCESS, message, data);
  }

  public static <T> Response<T> success(String message, T data) {
    return new Response<>("SUCCESS", ResponseStatus.SUCCESS, null, data);
  }

  public static <T> Response<T> error(String id, String message) {
    return new Response<>(id, ResponseStatus.ERROR, message, null);
  }

  public boolean isSuccess() {
    return status == ResponseStatus.SUCCESS;
  }

  public boolean isError() {
    return status == ResponseStatus.ERROR;
  }

  public ResponseStatus getStatus() {
    return status;
  }

  public String getId() {
    return id;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }

  @Override
  public String toString() {
    return "Response{" +
      "status=" + status +
      ", id='" + id + '\'' +
      ", message='" + message + '\'' +
      ", data=" + data +
      '}';
  }
}
