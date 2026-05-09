***

# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Realtime Online Auction System)

[![Java Version](https://img.shields.io/badge/Java-25-orange.svg)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-GUI-blue.svg)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36.svg)](https://maven.apache.org/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-4479A1.svg)](https://www.mysql.com/)

Dự án Hệ thống Đấu giá Trực tuyến (Online Auction System) là bài tập lớn thuộc học phần **Lập trình nâng cao**. Hệ thống mô phỏng một sàn giao dịch đấu giá thời gian thực (realtime), cho phép hàng ngàn người dùng kết nối đồng thời, tham gia trả giá với độ trễ cực thấp và đảm bảo tính toàn vẹn dữ liệu tuyệt đối (Thread-safety & ACID).

---

## 📑 Mục lục
1. [Giới thiệu dự án](#1-giới-thiệu-dự-án)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Kiến trúc hệ thống](#3-kiến-trúc-hệ-thống)
4. [Tính năng nổi bật (Điểm nhấn kỹ thuật)](#4-tính-năng-nổi-bật-điểm-nhấn-kỹ-thuật)
5. [Hướng dẫn cài đặt & Khởi chạy](#5-hướng-dẫn-cài-đặt--khởi-chạy)

---

## 1. Giới thiệu dự án

Hệ thống được thiết kế theo mô hình **Client-Server phân tầng**, áp dụng triệt để các nguyên lý Thiết kế Hướng đối tượng (OOP) và Design Patterns. Mã nguồn được chia thành 3 module độc lập để tối ưu hóa việc quản lý và tái sử dụng:

* 📦 **`auction-shared`**: Chứa các định nghĩa về Entity (User, Item, BidTransaction), Interfaces và giao thức giao tiếp (Request/Response Protocol) dùng chung cho cả 2 phía.
* 🖥️ **`auction-server`**: Trung tâm xử lý nghiệp vụ (Business Logic). Quản lý kết nối Socket đa luồng (Multi-threading), xử lý đồng bộ hóa (Concurrency Control) và tương tác với Cơ sở dữ liệu qua DAO Pattern.
* 💻 **`auction-client`**: Giao diện người dùng (Presentation Layer) xây dựng bằng JavaFX. Xử lý luồng sự kiện UI, validate dữ liệu và giao tiếp bất đồng bộ với Server qua TCP/IP.

---

## 2. Công nghệ sử dụng

| Hạng mục | Công nghệ / Thư viện áp dụng |
| :--- | :--- |
| **Ngôn ngữ lập trình** | Java (JDK 25) |
| **Giao diện (GUI)** | JavaFX (FXML, CSS tùy chỉnh giao diện Glassmorphism) |
| **Giao tiếp mạng** | Java Socket (TCP/IP), Java Object Serialization |
| **Cơ sở dữ liệu** | MySQL 8.0+, JDBC |
| **Connection Pool** | HikariCP (Tối ưu hóa kết nối DB, chống sập Server) |
| **Quản lý dự án** | Maven (Multi-module architecture) |
| **Lưu trữ hình ảnh** | Cloudinary REST API |
| **Logging & Testing** | SLF4J, Logback, JUnit 5, Mockito |

---

## 3. Kiến trúc hệ thống

* **Mô hình MVC (Model-View-Controller):** Áp dụng đồng bộ trên cả Client và Server. Client tách biệt hoàn toàn logic UI (Controllers) và logic mạng (`NetworkClient`). Server định tuyến các gói tin thông qua `ActionRegistry` và `ClientHandler`.
* **Quản lý Concurrent Bidding:** Sử dụng `ReentrantLock` cấp phát theo từng `itemId` kết hợp với các câu lệnh SQL Atomic (`UPDATE ... WHERE balance >= ?`). Đảm bảo tuyệt đối không xảy ra tình trạng Race Condition hay Lost Update khi hàng trăm người cùng đặt giá cho một sản phẩm trong cùng một tích tắc.
* **Realtime Broadcasting:** Server duy trì danh sách các luồng Socket đang mở (`ClientConnectionHub`). Khi có sự kiện (Có người trả giá cao hơn, Sản phẩm chốt đơn), Server chủ động đẩy (Push) gói tin `Response` về các Client liên quan để cập nhật UI ngay lập tức mà không cần Client phải Polling.

---

## 4. Tính năng nổi bật (Điểm nhấn kỹ thuật)

Bên cạnh các tính năng cơ bản (Đăng nhập/Đăng ký, Đăng bán, Lịch sử giao dịch), hệ thống tích hợp các thuật toán và cơ chế xử lý nâng cao:

### 🚀 4.1. Thuật toán Proxy Bidding (Đấu giá hộ) O(1)
Thay vì dùng vòng lặp mô phỏng từng bước giá gây tốn tài nguyên, hệ thống áp dụng thuật toán **Instant Resolution**. Khi có nhiều người cùng cài đặt Auto-bid, hệ thống sử dụng công thức toán học `Math.min(maxBidA, maxBidB) + increment` để tìm ra ngay người chiến thắng và mức giá hiện tại chỉ với **độ phức tạp O(1)** và 1 thao tác Database duy nhất.

### 🏆 4.2. Realtime Leaderboard (Bảng xếp hạng In-memory)
* Bảng xếp hạng "Top Đại Gia" được duy trì trực tiếp trên RAM của Server bằng cấu trúc dữ liệu `ConcurrentSkipListSet` giúp việc sắp xếp và lấy Top 10 luôn đạt hiệu suất **O(logN)**.
* Tự động đồng bộ hóa (Sync) Avatar khi người dùng thay đổi ảnh đại diện.
* **Bộ lọc thông minh:** Thuật toán tự động loại bỏ các tài khoản có Role là `ADMIN` khỏi bảng xếp hạng để đảm bảo tính công bằng.

### 🔍 4.3. Interactive Profile (Xem hồ sơ trực tiếp)
Giao diện Leaderboard trên JavaFX được gắn Event Listener. Khi người dùng **Click vào một dòng bất kỳ** trên bảng xếp hạng, Client sẽ gửi `Request.GET_USER_BY_ID` lên Server và sử dụng `SceneManager` để render trực tiếp màn hình Profile của người đó (hiển thị số phiên thắng, tổng tiền đã chi, độ uy tín,...).

### ⚡ 4.4. DelayQueue Settlement (Chốt phiên không nghẽn cổ chai)
Thay vì dùng Timer quét Database liên tục gây lãng phí tài nguyên, hệ thống đưa các sự kiện "Hết hạn đấu giá" vào một `DelayQueue` (In-memory Queue). Một Worker Thread duy nhất sẽ chờ (block) và chỉ thức dậy để xử lý chính xác vào mili-giây mà sản phẩm đó hết hạn.

### 🛡️ 4.5. Auto-Kill Port & Anti-Sniping
* **Auto-Kill Port 8080:** Server được lập trình để tự động giao tiếp với OS (Windows/Linux) qua `Runtime.getRuntime().exec()`, tìm và tiêu diệt (Kill Process) các tiến trình đang chiếm dụng Port 8080 trước khi khởi động, triệt tiêu hoàn toàn lỗi `BindException`.
* **Anti-Sniping:** Tự động cộng thêm 60 giây vào thời gian kết thúc nếu có bất kỳ lượt ra giá hợp lệ nào diễn ra trong 1 phút cuối cùng của phiên đấu giá.

---

## 5. Hướng dẫn cài đặt & Khởi chạy

### Bước 1: Chuẩn bị Cơ sở dữ liệu
1. Mở MySQL và tạo Database:
    ```sql
    CREATE DATABASE auction_db;
    ```
2. Cấu hình kết nối tại file: `auction-server/src/main/resources/db.properties`
    ```properties
    db.url=jdbc:mysql://localhost:3306/auction_db
    db.user=root
    db.password=your_password
    ```
*(Lưu ý: Hệ thống có cơ chế Database Migration, sẽ tự động tạo Bảng (Tables) và Cột (Columns) trong lần chạy đầu tiên).*

### Bước 2: Build toàn bộ dự án
Mở Terminal tại thư mục gốc của project và chạy lệnh Maven:
```bash
mvn clean verify
```

### Bước 3: Khởi chạy Server
Mở Terminal tại thư mục `auction-server` và chạy lệnh:
```bash
mvn exec:java -Dexec.mainClass="com.auction.server.Main"
```
*Server sẽ tự động dọn dẹp Port 8080, chạy Migration và thông báo `Server is running on port 8080`.*

### Bước 4: Khởi chạy Client
Mở một Terminal mới tại thư mục `auction-client` và chạy lệnh:
```bash
mvn javafx:run
```
*Để test tính năng Realtime và Concurrent Bidding, bạn có thể chạy lệnh trên ở nhiều cửa sổ Terminal khác nhau để mở nhiều Client cùng lúc.*
