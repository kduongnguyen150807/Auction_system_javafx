package com.auction.server.handler.auth;

import com.auction.server.dao.user.UserDao;
import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.user.OtpService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.util.Map;

public class ForgotPasswordHandler implements ActionHandler {
    private static final OtpService otpService = new OtpService();
    private final UserDao userDao = new UserDao();

    @Override
    public Response handle(Request request, HandlerContext context) {
        String action = request.getAction();

        if (Request.FORGOT_PASSWORD_REQ.equals(action)) {
            String email = (String) request.getPayload();
            if (!userDao.isEmailExists(email)) {
                return new Response(request.getRequestId(), Response.ERROR, "Email không tồn tại trong hệ thống!", null);
            }
            otpService.generateAndSendOtp(email);
            return new Response(request.getRequestId(), Response.OK, "Đã gửi mã OTP tới email của bạn.", null);
        }

        if (Request.FORGOT_PASSWORD_RESET.equals(action)) {
            Map<String, String> data = (Map<String, String>) request.getPayload();
            String email = data.get("email");
            String otp = data.get("otp");
            String newPassword = data.get("newPassword");

            if (otpService.verifyOtp(email, otp)) {
                userDao.updatePasswordByEmail(email, newPassword);
                return new Response(request.getRequestId(), Response.OK, "Đổi mật khẩu thành công!", null);
            } else {
                return new Response(request.getRequestId(), Response.ERROR, "Mã OTP sai hoặc đã hết hạn!", null);
            }
        }

        return new Response(request.getRequestId(), Response.ERROR, "Unknown action", null);
    }
}