package com.auction.client.Service;

import javafx.scene.layout.*;
import javafx.scene.image.Image;
import java.io.*;
import java.util.Properties;
import javafx.application.Platform;

public class BackGroundService {
    // 1. Dùng đường dẫn này để GHI (Lưu vĩnh viễn vào source code)
    private static final String SRC_CONFIG_PATH = "src/main/resources/config/bg_config.properties";

    // 2. Dùng đường dẫn này để ĐỌC (Lấy từ classpath)
    private static final String CLASSPATH_CONFIG_PATH = "/config/bg_config.properties";

    public static void updateConfigUrl(String newUrl) {
        Properties props = new Properties();
        File configFile = new File(SRC_CONFIG_PATH);

        try {
            // Đảm bảo thư mục tồn tại
            if (!configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }

            // BƯỚC 1: Đọc dữ liệu cũ từ file vật lý (nếu có)
            if (configFile.exists()) {
                try (FileInputStream in = new FileInputStream(configFile)) {
                    props.load(in);
                }
            }

            // BƯỚC 2: Cập nhật giá trị
            props.setProperty("bg_url", newUrl);

            // BƯỚC 3: Ghi dữ liệu xuống file vật lý
            try (FileOutputStream out = new FileOutputStream(configFile)) {
                props.store(out, "Update Background URL - " + java.time.LocalDateTime.now());
            }

            System.out.println("--- [Ghi File] Thành công: " + newUrl + " ---");

        } catch (IOException e) {
            System.err.println("Lỗi khi ghi file config: " + e.getMessage());
        }
    }

    public static void apply(Pane root) {
        if (root == null) {
            System.out.println("⚠️ Cảnh báo: Root Pane bị null, không thể áp dụng nền!");
            return;
        }

        Properties props = new Properties();
        String bgUrl = null;

        // ƯU TIÊN: Đọc trực tiếp từ file vật lý trước để cập nhật ngay lập tức (Real-time update)
        File physicalFile = new File(SRC_CONFIG_PATH);
        try {
            if (physicalFile.exists()) {
                try (FileInputStream fis = new FileInputStream(physicalFile)) {
                    props.load(fis);
                    bgUrl = props.getProperty("bg_url");
                }
            } else {
                // Nếu không thấy file vật lý (khi đã đóng gói JAR), đọc từ Resources
                try (InputStream is = BackGroundService.class.getResourceAsStream(CLASSPATH_CONFIG_PATH)) {
                    if (is != null) {
                        props.load(is);
                        bgUrl = props.getProperty("bg_url");
                    }
                }
            }

            if (bgUrl != null && !bgUrl.isEmpty()) {
                System.out.println("--- [Đọc File] URL tìm thấy: " + bgUrl + " ---");

                // Load ảnh với backgroundLoading = true
                Image img = new Image(bgUrl, true);

                img.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV.doubleValue() == 1.0 && !img.isError()) {
                        Platform.runLater(() -> {
                            BackgroundSize size = new BackgroundSize(100, 100, true, true, true, true);
                            BackgroundImage bgi = new BackgroundImage(
                                    img,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundRepeat.NO_REPEAT,
                                    BackgroundPosition.CENTER,
                                    size
                            );
                            root.setBackground(new Background(bgi));
                        });
                    }
                });

                // Nếu ảnh lỗi (đường dẫn sai), in ra để debug
                String finalBgUrl = bgUrl;
                img.errorProperty().addListener((obs, oldV, isError) -> {
                    if (isError) System.err.println("❌ Lỗi load ảnh từ URL: " + finalBgUrl);
                });
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi apply background: " + e.getMessage());
        }

    }
    public static String randomBg() {
        String urlToSave = "";
        try {
            // 1. Tạo link tạm để "hỏi" Server
            int randomId = new java.util.Random().nextInt(1000);
            String initialUrl = "https://picsum.photos/1600/900?random=" + randomId;

            // 2. Kết nối để lấy Link trực tiếp (Direct Link)
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(initialUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.connect();

            String realUrl = connection.getHeaderField("Location");

            // 3. Chốt link: Nếu có link direct thì lấy, không thì dùng link random có ID cố định
            urlToSave = (realUrl != null && !realUrl.isEmpty()) ? realUrl : initialUrl;

            // 4. Lưu vào file config ngay lập tức
            updateConfigUrl(urlToSave);

            System.out.println("--- Đã chốt và lưu link: " + urlToSave + " ---");

        } catch (Exception e) {
            urlToSave = "https://picsum.photos/1600/900?random=" + new java.util.Random().nextInt(1000);
            updateConfigUrl(urlToSave);
        }
        return urlToSave;
    }
}