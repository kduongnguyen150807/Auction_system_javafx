package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.UserDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;
import com.auction.shared.user.User;
import com.auction.shared.user.UserStatus;

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

    User user = context.getDaoContext().getDao(UserDao.class).login(username, password);
    if (user == null) {
      return new Response(request.getRequestId(), Response.ERROR, "your account might be ur girlfriend, who doesnt exists", null);
    } else if (user.getStatus().equals(UserStatus.LOCKED)) {
      return new Response(request.getRequestId(), Response.ERROR, "your acount is black pal", null);
    }

    return new Response(request.getRequestId(), Response.OK, "success", user);
  }
}
