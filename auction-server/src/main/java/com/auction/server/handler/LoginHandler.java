package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;
import com.auction.shared.user.User;

import java.util.Map;

/**
 * Handler xử lý yêu cầu đăng nhập.
 *
 * <p>Chức năng:
 * <ul>
 *   <li>Đọc thông tin username và password từ payload của {@link Request}</li>
 *   <li>Gọi {@link com.auction.server.dao.UserDao} để xác thực người dùng</li>
 *   <li>Nếu thành công: lưu user vào {@link HandlerContext}</li>
 *   <li>Trả về {@link Response} tương ứng</li>
 * </ul>
 *
 * <p>Yêu cầu payload phải là {@code Map<String, String>} chứa:
 * <ul>
 *   <li>"username"</li>
 *   <li>"password"</li>
 * </ul>
 */
public class LoginHandler implements RequestHandler {
  @Override
  public Response handle(Request request, HandlerContext context) {
    Map<String, String> credentials = (Map<String, String>) request.getPayload();
    String username = credentials.get("username");
    String password = credentials.get("password");

    User user = context.getDaoContext().getUserDao().login(username, password);
    if (user != null) {
      context.setUser(user);
      return new Response(request.getRequestId(), Response.OK, "success", user);
    } else {
      return new Response(request.getRequestId(), Response.ERROR, "fail", null);
    }
  }
}
