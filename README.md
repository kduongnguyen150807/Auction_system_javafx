# Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

[![CI/CD Pipeline](https://github.com/kduongnguyen150807/Auction_system_javafx/actions/workflows/build.yml/badge.svg)](https://github.com/YOUR_USERNAME/YOUR_REPO/actions)

Dự án phát triển hệ thống đấu giá trực tuyến thuộc bài tập lớn môn Lập trình nâng cao. Hệ thống mô phỏng môi trường đấu giá thời gian thực, cho phép nhiều người dùng (concurrent users) tham gia cạnh tranh giá sản phẩm với độ trễ thấp và đảm bảo tính toàn vẹn dữ liệu.

Dự án áp dụng chặt chẽ kiến trúc Client-Server phân tầng, thiết kế hướng đối tượng (OOP) và các Design Pattern chuẩn mực trong công nghiệp phần mềm.

## 1. Kiến Trúc Hệ Thống (Architecture)

Hệ thống được thiết kế theo mô hình 3 modules độc lập nhằm tối ưu hóa việc quản lý mã nguồn và tái sử dụng:

* **auction-shared:** Chứa các định nghĩa về Entity (User, Item, BidTransaction), Interfaces và các gói dữ liệu giao tiếp chung.
* **auction-server:** Đóng vai trò là trung tâm xử lý nghiệp vụ (Business Logic). Áp dụng mô hình MVC, quản lý kết nối Socket đa luồng (Multi-threading) và trực tiếp thao tác với cơ sở dữ liệu qua các lớp DAO. (host phải có inbound rule cho port 8080 hoặc port host)
* **auction-client:** Đóng vai trò giao diện người dùng (Presentation Layer). Áp dụng mô hình MVC với JavaFX, xử lý luồng sự kiện UI và giao tiếp với Server qua giao thức TCP/IP.

## 2. Công Nghệ Sử Dụng (Tech Stack)

* **Nền tảng & Ngôn ngữ:** Java (JDK 25)
* **Giao diện (GUI):** JavaFX, FXML, CSS
* **Cơ sở dữ liệu:** MySQL (JDBC)
* **Mạng & Giao tiếp:** Java Socket (TCP), Cloudinary REST API (Image Hosting)
* **Build Tool & CI/CD:** Maven, GitHub Actions
* **Testing:** JUnit 5 (Unit Testing)

## 3. Thiết Kế Hệ Thống & Cấu Trúc Dữ Liệu

Dự án triển khai nghiêm ngặt các nguyên lý OOP và Design Patterns để giải quyết các bài toán phức tạp:

* **Design Patterns:**
  * *Factory Method:* Được áp dụng trong `ItemFactory` để khởi tạo linh hoạt các loại tài sản đấu giá (Vehicle, Electronics, Art).
  * *Singleton:* Quản lý các tài nguyên duy nhất như `DatabaseConnection`, `AuctionManager`, và `ClientSession`.
  * *Observer (Biến thể qua Socket):* Cập nhật trạng thái và giá đấu theo thời gian thực (Realtime Broadcasting) tới toàn bộ Clients kết nối.
* **Tính Đa hình (Polymorphism):** Lớp trừu tượng `Item` định nghĩa phương thức `calculatetax()`, cho phép các lớp con (Vehicle, Art, Electronics) tự triển khai logic tính thuế khác biệt.
* **Cấu trúc dữ liệu & Thuật toán:**
  * Sử dụng `PriorityQueue` kết hợp với thuật toán so sánh `maxBid` để vận hành hệ thống Auto-Bidding, đảm bảo thứ tự ưu tiên trúng thầu chính xác.
  * Cấu trúc luồng an toàn (Thread-safe) với `ConcurrentHashMap` và `CopyOnWriteArrayList` trong môi trường đa luồng.

## 4. Chức Năng Kỹ Thuật Cốt Lõi

* **Concurrent Bidding & Thread Safety:** Xử lý đấu giá đồng thời an toàn với từ khóa `synchronized`, ngăn chặn triệt để tình trạng Race Condition, Lost Update hoặc tranh chấp dữ liệu khi nhiều người cùng đặt giá.
* **Auto-Bidding:** Cho phép người dùng thiết lập giá trần (`maxBid`) và bước giá (`increment`). Hệ thống tự động proxy đấm giá thay mặt người dùng dựa trên hàng đợi ưu tiên.
* **Anti-Sniping:** Tự động bù giờ (cộng thêm 60 giây) nếu có giao dịch đặt giá hợp lệ trong những giây cuối cùng của phiên đấu giá.
* **Realtime Analytics:** Vẽ biểu đồ đường (Line Chart) mô tả lịch sử biến động giá của sản phẩm theo thời gian thực.
* **System Integration:** Tích hợp `java.awt.SystemTray` để đẩy thông báo (Push Notifications) trực tiếp ra màn hình hệ điều hành của người dùng khi bị vượt giá.

## 5. Hướng Dẫn Cài Đặt (Setup Instructions)

**Yêu cầu hệ thống:**

* JDK 25 trở lên.
* Maven 3.8+
* MySQL Server 8.0+

**Bước 1: Khởi tạo Cơ sở dữ liệu**

1. Mở hệ quản trị MySQL.
2. Tạo database mới: `CREATE DATABASE auction_db;`
3. Import file dữ liệu mẫu (nếu có) hoặc để hệ thống DAO tự động tạo bảng (Alter Table) khi chạy lần đầu.
4. Cập nhật thông tin cấu hình (URL, Username, Password) tại lớp `DatabaseConnection` trong module `auction-server`.

**Bước 2: Build dự án**
Mở Terminal tại thư mục gốc của project và chạy lệnh:

```bash
mvn clean verify
```

**Bước 3: Khởi chạy Server**
Mở Terminal tại thư mục `auction-server` và thực thi:

```bash
mvn exec:java -Dexec.mainClass="com.auction.server.Main"
```

*(Đảm bảo Server báo khởi tạo thành công và đang lắng nghe ở cổng mặc định).*

**Bước 4: Khởi chạy Client**
Mở Terminal mới tại thư mục `auction-client` và thực thi:

```bash
mvn clean javafx:run
```

*(Để test concurrent bidding, có thể chạy lệnh này trên nhiều cửa sổ Terminal khác nhau để mở nhiều Client song song).*

## 6. Quy Định Phát Triển (Dành cho thành viên)

1. **Branching:** Không push trực tiếp lên nhánh `main`. Phân nhánh theo tính năng (VD: `feat/realtime-chart`, `fix/login-bug`) và merge qua Pull Request.
2. **Commit Message:** Sử dụng chuẩn Conventional Commits (`feat:`, `fix:`, `refactor:`).
3. **CI/CD Quality Gate:** Mọi nhánh trước khi merge phải vượt qua toàn bộ Unit Tests trên GitHub Actions pipeline.
