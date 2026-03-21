package com.auction.client.controller;

import com.auction.shared.Request;
import com.auction.shared.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class AuthClientController {
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    //Format của email
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public AuthClientController(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
    }

    public Response login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                hwid == null || hwid.trim().isEmpty()) {
            // Trả về lỗi
            return new Response("Có vấn đề", Response.err, "Vui lòng nhập đầy đủ thông tin", null);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("hwid", hwid);

        // Tạo request yêu cầu đăng nhập
        Request request = new Request(Request.login, payload);
        return sendToServer(request);
    }
    //Xử lý phần đăng ký
    public Response register(String username, String password, String confirmPassword, String email, String role) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty() ||
                email == null || email.trim().isEmpty()) {
            return new Response("local", Response.err, "Vui lòng điền đầy đủ các trường", null);
        }

        if (!password.equals(confirmPassword)) {
            return new Response("local", Response.err, "Mật khẩu nhập lại không khớp", null);
        }
        if (username.length() < 3 || username.length() > 20) {
            return new Response("local", Response.err, "Tên đăng nhập phải từ 3-20 ký tự", null);
        }
        if (username.contains(" ")) {
            return new Response("local", Response.err, "Tên đăng nhập không được chứa khoảng trắng", null);
        }
        if (password.length() < 6) {
            return new Response("local", Response.err, "Mật khẩu phải có ít nhất 6 ký tự", null);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return new Response("local", Response.err, "Định dạng email không hợp lệ", null);
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);
        payload.put("email", email);
        payload.put("role", role != null ? role : "bidder");

        // Tạo request yêu cầu đăng ký
        Request request = new Request(Request.signup, payload);
        return sendToServer(request);
    }

    private Response sendToServer(Request request) {
        try {
            out.writeObject(request);//Thả vào outputstream request
            out.flush();

            Object obj = in.readObject();//Há miệng chờ Server xử lý -> Trả về cái gì đó
            if (obj instanceof Response response) {
                return response;
            }
            return new Response(request.getid(), Response.err, "Định dạng phản hồi từ server không hợp lệ", null);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new Response(request.getid(), Response.err, "Mất kết nối tới máy chủ", null);
        }
    }
}