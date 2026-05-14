package com.auction.server.service.user;

import java.io.InputStream;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.*;
import javax.mail.internet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OtpService {
    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);

    private String emailUser;
    private String emailPassword;

    // Lưu OTP trên RAM: Key = Email, Value = Record(OTP, ExpireTime)
    private final ConcurrentHashMap<String, OtpRecord> otpCache = new ConcurrentHashMap<>();

    private record OtpRecord(String code, long expireTimeMs) {}

    public OtpService() {
        loadMailConfig();
    }

    // Đọc cấu hình từ file db.properties
    private void loadMailConfig() {
        try {
            Properties props = new Properties();
            InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties");
            if (input != null) {
                props.load(input);
            }
            // Ưu tiên biến môi trường (nếu deploy lên server thật), nếu không có thì lấy trong file properties
            this.emailUser = System.getenv("MAIL_USER") != null ? System.getenv("MAIL_USER") : props.getProperty("mail.user");
            this.emailPassword = System.getenv("MAIL_PASS") != null ? System.getenv("MAIL_PASS") : props.getProperty("mail.password");
        } catch (Exception e) {
            logger.error("Không thể đọc cấu hình Email từ db.properties", e);
        }
    }

    public void generateAndSendOtp(String targetEmail) {
        // Tạo mã 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(999999));
        // Hết hạn sau 5 phút
        otpCache.put(targetEmail, new OtpRecord(otp, System.currentTimeMillis() + 5 * 60 * 1000));

        // Gửi email BẤT ĐỒNG BỘ (Không block luồng Socket)
        CompletableFuture.runAsync(() -> sendEmail(targetEmail, otp));
    }

    public boolean verifyOtp(String email, String inputOtp) {
        OtpRecord record = otpCache.get(email);
        if (record == null) return false;

        if (System.currentTimeMillis() > record.expireTimeMs) {
            otpCache.remove(email); // Xóa nếu hết hạn
            return false;
        }

        if (record.code.equals(inputOtp)) {
            otpCache.remove(email); // Dùng xong xóa luôn (One-time)
            return true;
        }
        return false;
    }

    private void sendEmail(String to, String otp) {
        if (emailUser == null || emailPassword == null) {
            logger.error("Chưa cấu hình mail.user hoặc mail.password trong db.properties!");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailUser, emailPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailUser));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject("Bidding88 - Mã xác nhận khôi phục mật khẩu");

            String htmlContent = "<h3>Xin chào,</h3>"
                    + "<p>Bạn vừa yêu cầu khôi phục mật khẩu tại hệ thống đấu giá <b>Bidding88</b>.</p>"
                    + "<p>Mã OTP của bạn là: <b style='font-size: 20px; color: #ffaa00;'>" + otp + "</b></p>"
                    + "<p><i>Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ cho bất kỳ ai.</i></p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            logger.info("Đã gửi OTP tới email: {}", to);
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email tới {}: {}", to, e.getMessage());
        }
    }
}