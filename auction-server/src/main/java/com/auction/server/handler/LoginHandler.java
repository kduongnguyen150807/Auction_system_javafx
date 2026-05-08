package com.auction.server.handler;

import com.auction.server.context.HandlerContext;
import com.auction.server.dao.UserDao;
import com.auction.server.utils.RequestHandler;
import com.auction.shared.dto.LoginCredentials;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.Response;
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
public class LoginHandler implements RequestHandler<LoginCredentials, User> {
  @Override
  public Response<User> handle(Request<LoginCredentials> request, HandlerContext context) {
    LoginCredentials loginCredentials =  request.getData();

    User user = context.getDaoContext().getDao(UserDao.class).login(loginCredentials.getUsername(), loginCredentials.getPassword());

    if (user == null) {
      return Response.error(request.getId(), "your account might be ur girlfriend, who doesnt exists");
    } else if (user.getStatus().equals(UserStatus.LOCKED)) {
      return Response.error(request.getId(),  "your acount is black pal");
    }

    context.setUser(user);
    return Response.success(request.getId(), "success", user);
  }
}
