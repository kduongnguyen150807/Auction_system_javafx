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

    private static final long OTP_EXPIRE_TIME_MS = 5 * 60 * 1000;
    private static final long RESEND_COOLDOWN_MS = 60 * 1000;

    private String emailUser;
    private String emailPassword;

    // Lưu OTP trên RAM: Key = Email, Value = Record(OTP, ExpireTime)
    private final ConcurrentHashMap<String, OtpRecord> otpCache = new ConcurrentHashMap<>();

    // Lưu thời điểm gửi OTP gần nhất: Key = Email, Value = thời gian gửi gần nhất
    private final ConcurrentHashMap<String, Long> lastSentAt = new ConcurrentHashMap<>();

    private record OtpRecord(String code, long expireTimeMs) {}

    public OtpService() {
        loadMailConfig();
    }

    // Đọc cấu hình từ file db.properties
    private void loadMailConfig() {
        try {
            Properties props = new Properties();

            try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
                if (input != null) {
                    props.load(input);
                }
            }

            // Ưu tiên biến môi trường nếu deploy lên server thật,
            // nếu không có thì lấy trong file properties
            this.emailUser =
                    System.getenv("MAIL_USER") != null
                            ? System.getenv("MAIL_USER")
                            : props.getProperty("mail.user");

            this.emailPassword =
                    System.getenv("MAIL_PASS") != null
                            ? System.getenv("MAIL_PASS")
                            : props.getProperty("mail.password");
        } catch (Exception e) {
            logger.error("Không thể đọc cấu hình Email từ db.properties", e);
        }
    }

    public boolean canSendOtp(String targetEmail) {
        String email = normalizeEmail(targetEmail);

        if (email.isEmpty()) {
            return false;
        }

        Long lastSentTime = lastSentAt.get(email);
        if (lastSentTime == null) {
            return true;
        }

        return System.currentTimeMillis() - lastSentTime >= RESEND_COOLDOWN_MS;
    }

    public long getRemainingCooldownSeconds(String targetEmail) {
        String email = normalizeEmail(targetEmail);

        if (email.isEmpty()) {
            return 60;
        }

        Long lastSentTime = lastSentAt.get(email);
        if (lastSentTime == null) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastSentTime;
        long remaining = RESEND_COOLDOWN_MS - elapsed;

        if (remaining <= 0) {
            return 0;
        }

        return (remaining + 999) / 1000;
    }

    public void generateAndSendOtp(String targetEmail) {
        String email = normalizeEmail(targetEmail);

        if (email.isEmpty()) {
            return;
        }

        // Tạo mã 6 số ngẫu nhiên
        String otp = String.format("%06d", new Random().nextInt(1_000_000));

        // Hết hạn sau 5 phút
        otpCache.put(email, new OtpRecord(otp, System.currentTimeMillis() + OTP_EXPIRE_TIME_MS));

        // Ghi nhận thời điểm gửi OTP gần nhất
        lastSentAt.put(email, System.currentTimeMillis());

        // Gửi email BẤT ĐỒNG BỘ, không block luồng Socket
        CompletableFuture.runAsync(() -> sendEmail(email, otp));
    }

    public boolean verifyOtp(String email, String inputOtp) {
        String normalizedEmail = normalizeEmail(email);
        OtpRecord record = otpCache.get(normalizedEmail);

        if (record == null) {
            return false;
        }

        if (System.currentTimeMillis() > record.expireTimeMs) {
            otpCache.remove(normalizedEmail);
            return false;
        }

        if (record.code.equals(inputOtp)) {
            otpCache.remove(normalizedEmail);
            lastSentAt.remove(normalizedEmail);
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
                    + "<p>Mã OTP của bạn là: <b style='font-size: 20px; color: #ffaa00;'>"
                    + otp
                    + "</b></p>"
                    + "<p><i>Mã này sẽ hết hạn sau 5 phút. Vui lòng không chia sẻ cho bất kỳ ai.</i></p>";

            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            logger.info("Đã gửi OTP tới email: {}", to);
        } catch (MessagingException e) {
            logger.error("Lỗi gửi email tới {}: {}", to, e.getMessage());
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}