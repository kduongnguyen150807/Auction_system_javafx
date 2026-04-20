package com.auction.client.controller;

import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthController {
  private final ObjectOutputStream out;
  private final ObjectInputStream in;
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

  public static boolean isValidEmail(String email) {
    return email != null && !email.trim().isEmpty() && EMAIL_PATTERN.matcher(email).matches();
  }

  public AuthController(ObjectOutputStream out, ObjectInputStream in) {
    this.out = out;
    this.in = in;
  }

  public Response login(String username, String password) {
    if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
      return new Response("local", Response.ERROR, "Vui lòng nhập đầy đủ thông tin", null);
    }
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("password", password);
    Request request = new Request(Request.LOGIN, payload);
    return sendToServer(request);
  }

  public Response register(String username, String password, String confirmPassword, String email, String age) {
    if (username == null || username.trim().isEmpty()
        || password == null || password.trim().isEmpty()
        || confirmPassword == null || confirmPassword.trim().isEmpty()
        || email == null || email.trim().isEmpty()
        || age == null || age.trim().isEmpty()) {
      return new Response("local", Response.ERROR, "Vui lòng điền đầy đủ các trường", null);
    }
    if (!password.equals(confirmPassword)) {
      return new Response("local", Response.ERROR, "Mật khẩu nhập lại không khớp", null);
    }
    if (username.length() < 3 || username.length() > 20) {
      return new Response("local", Response.ERROR, "Tên đăng nhập phải từ 3-20 ký tự", null);
    }
    if (username.contains(" ")) {
      return new Response("local", Response.ERROR, "Tên đăng nhập không được chứa khoảng trắng", null);
    }
    if (password.length() < 6) {
      return new Response("local", Response.ERROR, "Mật khẩu phải có ít nhất 6 ký tự", null);
    }
    if (!AuthController.isValidEmail(email)) {
      return new Response("local", Response.ERROR, "Định dạng email không hợp lệ", null);
    }

    Map<String, Object> payload = new HashMap<>();
    payload.put("username", username);
    payload.put("password", password);
    payload.put("email", email);
    payload.put("age", age);
    Request request = new Request(Request.SIGNUP, payload);
    return sendToServer(request);
  }

  private Response sendToServer(Request request) {
    try {
      out.writeObject(request);
      out.flush();
      Object obj = in.readObject();
      if (obj instanceof Response response) {
        return response;
      }
      return new Response(request.getRequestId(), Response.ERROR,
          "Định dạng phản hồi từ server không hợp lệ", null);
    } catch (IOException | ClassNotFoundException e) {
      return new Response(request.getRequestId(), Response.ERROR, "Mất kết nối tới máy chủ", null);
    }
  }
}
