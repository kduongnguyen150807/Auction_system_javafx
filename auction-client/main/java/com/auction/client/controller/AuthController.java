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
    private static final Pattern p = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    public AuthClientController(ObjectOutputStream out, ObjectInputStream in) {
        this.out = out;
        this.in = in;
    }
    public Response login(String u, String pass) {
        if (u == null || u.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            return new Response("local", Response.err, "Vui lòng nhập đầy đủ thông tin", null);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", u);
        payload.put("password", pass);
        Request request = new Request(Request.login, payload);
        Response ans = sendToServer(request);
        return ans;
    }
    public Response register(String u, String pass, String cp, String e, String age) {
        if (u == null || u.trim().isEmpty() || pass == null || pass.trim().isEmpty() || cp == null || cp.trim().isEmpty() || e == null || e.trim().isEmpty() || age == null || age.trim().isEmpty()) {
            return new Response("local", Response.err, "Vui lòng điền đầy đủ các trường", null);
        }
        if (!pass.equals(cp)) {
            return new Response("local", Response.err, "Mật khẩu nhập lại không khớp", null);
        }
        if (u.length() < 3 || u.length() > 20) {
            return new Response("local", Response.err, "Tên đăng nhập phải từ 3-20 ký tự", null);
        }
        if (u.contains(" ")) {
            return new Response("local", Response.err, "Tên đăng nhập không được chứa khoảng trắng", null);
        }
        if (pass.length() < 6) {
            return new Response("local", Response.err, "Mật khẩu phải có ít nhất 6 ký tự", null);
        }
        if (!AuthClientController.p.matcher(e).matches()) {
            return new Response("local", Response.err, "Định dạng email không hợp lệ", null);
        }
        Map<String, Object> payload = new HashMap<>();
        payload.put("username", u);
        payload.put("password", pass);
        payload.put("email", e);
        payload.put("age", age);
        Request request = new Request(Request.signup, payload);
        Response ans = sendToServer(request);
        return ans;
    }
    private Response sendToServer(Request request) {
        try {
            out.writeObject(request);
            out.flush();
            Object obj = in.readObject();
            if (obj instanceof Response res) {
                return res;
            }
            return new Response(request.getid(), Response.err, "Định dạng phản hồi từ server không hợp lệ", null);
        } catch (IOException | ClassNotFoundException e) {
            return new Response(request.getid(), Response.err, "Mất kết nối tới máy chủ", null);
        }
    }
}