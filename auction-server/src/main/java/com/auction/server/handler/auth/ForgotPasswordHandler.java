package com.auction.server.handler.auth;

import com.auction.server.handler.dispatch.ActionHandler;
import com.auction.server.handler.dispatch.HandlerContext;
import com.auction.server.service.user.OtpService;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForgotPasswordHandler implements ActionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ForgotPasswordHandler.class);

    private static final OtpService otpService = new OtpService();

    @Override
    public Response handle(Request request, HandlerContext context) {
        try {
            String action = request.getAction();

            if (Request.FORGOT_PASSWORD_REQ.equals(action)) {
                return handleOtpRequest(request, context);
            }

            if (Request.FORGOT_PASSWORD_RESEND.equals(action)) {
                return handleResendOtp(request, context);
            }

            if (Request.FORGOT_PASSWORD_RESET.equals(action)) {
                return handlePasswordReset(request, context);
            }

            return new Response(request.getRequestId(), Response.ERROR, "unknown_action", null);
        } catch (ClassCastException e) {
            LOGGER.warn("Forgot password payload cast failed: {}", e.getMessage());
            return new Response(request.getRequestId(), Response.ERROR, "invalid_payload", null);
        } catch (Exception e) {
            LOGGER.error("Forgot password error", e);
            return new Response(request.getRequestId(), Response.ERROR, "server_error", null);
        }
    }

    private Response handleOtpRequest(Request request, HandlerContext context) {
        return sendOtpToEmail(
                request,
                context,
                "Đã gửi mã OTP tới email của bạn.");
    }

    private Response handleResendOtp(Request request, HandlerContext context) {
        return sendOtpToEmail(
                request,
                context,
                "Đã gửi lại mã OTP tới email của bạn.");
    }

    private Response sendOtpToEmail(
            Request request,
            HandlerContext context,
            String successMessage) {
        String email = (String) request.getPayload();

        if (isBlank(email)) {
            return new Response(request.getRequestId(), Response.ERROR, "missing_email", null);
        }

        String normalizedEmail = email.trim();

        if (!context.getUserService().isEmailExists(normalizedEmail)) {
            return new Response(
                    request.getRequestId(),
                    Response.ERROR,
                    "Email không tồn tại trong hệ thống!",
                    null);
        }

        otpService.generateAndSendOtp(normalizedEmail);

        return new Response(
                request.getRequestId(),
                Response.OK,
                successMessage,
                null);
    }

    private Response handlePasswordReset(Request request, HandlerContext context) {
        @SuppressWarnings("unchecked")
        Map<String, String> data = (Map<String, String>) request.getPayload();

        String payloadError = validateResetPayload(data);
        if (payloadError != null) {
            return new Response(request.getRequestId(), Response.ERROR, payloadError, null);
        }

        String email = data.get("email").trim();
        String otp = data.get("otp").trim();
        String newPassword = data.get("newPassword");

        if (!otpService.verifyOtp(email, otp)) {
            return new Response(
                    request.getRequestId(),
                    Response.ERROR,
                    "Mã OTP sai hoặc đã hết hạn!",
                    null);
        }

        String hashedPassword = sha256(newPassword);
        boolean updated = context.getUserService().updatePasswordByEmail(email, hashedPassword);

        return new Response(
                request.getRequestId(),
                updated ? Response.OK : Response.ERROR,
                updated ? "Đổi mật khẩu thành công!" : "update_failed",
                null);
    }

    private static String validateResetPayload(Map<String, String> data) {
        if (data == null) {
            return "invalid_payload";
        }

        if (isBlank(data.get("email"))
                || isBlank(data.get("otp"))
                || isBlank(data.get("newPassword"))) {
            return "missing_required_fields";
        }

        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : encoded) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash password", e);
        }
    }
}