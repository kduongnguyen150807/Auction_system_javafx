# ⚡ Hệ Thống Đấu Giá Trực Tuyến - Auction System JavaFX

![Java](https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-GUI-1565C0?style=for-the-badge&logo=java&logoColor=white)
![Socket](https://img.shields.io/badge/TCP%2FIP-Socket-4B0082?style=for-the-badge)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

## Chạy nhanh bằng executable fat JAR

Repository đã cấu hình `maven-shade-plugin` cho cả server và client để đóng gói dependency vào JAR chạy trực tiếp.

```bash
mvn clean package -DskipTests
```

Sau khi build, có thể chạy bằng file JAR ở thư mục gốc:

```bash
java -jar server.jar
java -jar client.jar
```

Hoặc chạy trực tiếp từ thư mục `target`:

```bash
java -jar auction-server/target/auction-server.jar
java -jar auction-client/target/auction-client.jar
```

Trước khi chạy server, cần tạo MySQL database và chỉnh `auction-server/src/main/resources/db.properties`.

## 1. Mô tả bài toán và phạm vi hệ thống

**Auction System JavaFX** là hệ thống đấu giá trực tuyến được xây dựng bằng Java theo mô hình **Client - Server**.

Hệ thống mô phỏng một sàn đấu giá online, trong đó người dùng có thể đăng ký tài khoản, đăng nhập, xem danh sách sản phẩm, đăng sản phẩm đấu giá, đặt giá, theo dõi sản phẩm, xem lịch sử giao dịch, chat, đánh giá và quản lý thông tin cá nhân.

Mô hình tổng quát của hệ thống:

```text
auction-client  <---- TCP Socket port 8080 ---->  auction-server  <----> MySQL
```

Phạm vi hệ thống gồm:

- Ứng dụng **client** sử dụng JavaFX để hiển thị giao diện người dùng.
- Ứng dụng **server** xử lý nghiệp vụ, quản lý kết nối client và giao tiếp với database.
- Database **MySQL** lưu trữ thông tin người dùng, sản phẩm, phiên đấu giá, lịch sử đặt giá, giao dịch, đánh giá và chat.
- Client và server giao tiếp với nhau thông qua **TCP Socket**.
- Hệ thống hỗ trợ nhiều client kết nối cùng lúc để mô phỏng nhiều người dùng đấu giá.

---

## 2. Công nghệ sử dụng, môi trường chạy và yêu cầu cài đặt

### 2.1. Công nghệ sử dụng

Dự án sử dụng các công nghệ chính:

| Thành phần | Công nghệ sử dụng |
| :--- | :--- |
| Ngôn ngữ lập trình | Java 25 |
| Giao diện người dùng | JavaFX, FXML, CSS |
| Quản lý project | Maven multi-module |
| Giao tiếp mạng | TCP Socket |
| Database | MySQL 8.0+ |
| Kết nối database | JDBC, HikariCP |
| Logging | SLF4J, Logback |
| Đóng gói JAR | Maven Shade Plugin |
| Kiểm thử | JUnit |

### 2.2. Yêu cầu môi trường

Máy chạy chương trình cần cài:

- JDK 25
- Maven 3.8 trở lên
- MySQL 8.0 trở lên
- IntelliJ IDEA hoặc IDE hỗ trợ Maven

Kiểm tra Java:

```bash
java -version
```

Kiểm tra Maven:

```bash
mvn -version
```

---

## 3. Cấu trúc thư mục và các module chính

Dự án được tổ chức theo dạng **Maven multi-module**, gồm 3 module chính:

```text
Auction_system_javafx
│
├── pom.xml
│
├── auction-shared
│   ├── pom.xml
│   └── src/main/java
│
├── auction-server
│   ├── pom.xml
│   ├── src/main/java
│   └── src/main/resources
│
└── auction-client
    ├── pom.xml
    ├── src/main/java
    └── src/main/resources
```

### 3.1. Module `auction-shared`

Module `auction-shared` chứa các class dùng chung giữa client và server.

Một số class tiêu biểu:

```text
Request
Response
User
Item
BidTransaction
Rating
ChatMessage
Friendship
UserRole
AuctionType
ItemStatus
```

Vai trò của module này là thống nhất dữ liệu khi client và server giao tiếp qua socket.

---

### 3.2. Module `auction-server`

Module `auction-server` là phần server của hệ thống.

Cấu trúc chính:

```text
auction-server
│
├── controller
│   ├── SocketServer
│   └── ClientHandler
│
├── handler
│   ├── auth
│   ├── auction
│   ├── chat
│   ├── rating
│   ├── user
│   └── misc
│
├── service
│   ├── auction
│   └── user
│
└── dao
    ├── auction
    ├── user
    ├── chat
    ├── rating
    ├── wallet
    └── platform
```

Vai trò một số thành phần chính:

- `Main`: điểm khởi chạy server.
- `SocketServer`: mở server socket và chờ client kết nối.
- `ClientHandler`: xử lý request của từng client.
- `ActionRegistry`: điều phối request đến đúng handler.
- `LoginHandler`, `SignupHandler`: xử lý đăng nhập và đăng ký.
- `BidHandler`: xử lý đặt giá.
- `AuctionManager`: quản lý nghiệp vụ đấu giá.
- `DatabaseConnection`: quản lý kết nối MySQL.
- `DatabaseMigration`: tự động tạo/cập nhật bảng trong database.

---

### 3.3. Module `auction-client`

Module `auction-client` là phần giao diện người dùng bằng JavaFX.

Cấu trúc chính:

```text
auction-client
│
├── controller
│   ├── WelcomeController
│   ├── LoginController
│   ├── RegisterController
│   └── ForgotPasswordController
│
├── network
│   ├── NetworkClient
│   ├── ObjectSocketConnection
│   └── IncomingResponseRouter
│
├── service
│   ├── BiddingClientService
│   ├── LotSubmissionService
│   └── UserAccountService
│
├── ui
│   ├── Main
│   ├── TrangChu
│   ├── ItemCard
│   ├── ItemInformation
│   ├── BiddingForm
│   ├── AddNewLot
│   ├── Profile
│   ├── Watchlist
│   ├── YourItem
│   ├── History
│   ├── TransactionHistory
│   ├── Chat
│   ├── RatingForm
│   └── SearchBar
│
└── util
```

Vai trò một số thành phần chính:

- `App`: launcher dùng khi chạy bằng file `.jar`.
- `Main`: khởi chạy JavaFX Application.
- `SceneManager`: quản lý chuyển màn hình.
- `NetworkClient`: gửi request từ client lên server.
- `IncomingResponseRouter`: nhận và xử lý response từ server.
- `KhungController`: controller chính sau khi đăng nhập.
- `TrangChuController`: điều khiển trang chủ.
- `ItemInformationController`: hiển thị chi tiết sản phẩm.
- `BiddingFormController`: xử lý giao diện đặt giá.
- `AddNewLotController`: xử lý thêm sản phẩm/lô đấu giá.
- `ProfileController`: quản lý hồ sơ người dùng.
- `ChatPageController`: xử lý giao diện chat.

---

## 4. Cấu hình database

Trước khi chạy server, cần tạo database trong MySQL:

```sql
CREATE DATABASE IF NOT EXISTS auction_db;
```

Sau đó kiểm tra file cấu hình database:

```text
auction-server/src/main/resources/db.properties
```

Ví dụ cấu hình:

```properties
db.url=jdbc:mysql://localhost:3306/auction_db
db.user=root
db.password=your_password
```

Trong đó:

- `db.url`: đường dẫn đến database MySQL.
- `db.user`: tài khoản MySQL.
- `db.password`: mật khẩu MySQL.

Ví dụ nếu MySQL dùng tài khoản `root` và mật khẩu `123456`:

```properties
db.url=jdbc:mysql://localhost:3306/auction_db
db.user=root
db.password=123456
```

Sau khi server kết nối database thành công, hệ thống sẽ tự chạy migration để tạo/cập nhật các bảng cần thiết.

---

## 5. Build project thành file JAR

Tại thư mục gốc project, chạy lệnh:

```bash
mvn clean package -DskipTests "-Dcheckstyle.skip=true"
```

Nếu build thành công, terminal sẽ hiển thị:

```text
BUILD SUCCESS
```

Dự án sử dụng `maven-shade-plugin` để đóng gói dependencies vào file JAR, giúp chương trình có thể chạy trực tiếp bằng lệnh:

```bash
java -jar <ten-file>.jar
```

---

## 6. Vị trí các file `.jar`

Sau khi build thành công, các file JAR nằm tại:

```text
auction-server/target/auction-server.jar
auction-client/target/auction-client.jar
```

Trong đó:

- `auction-server.jar`: file chạy server.
- `auction-client.jar`: file chạy client JavaFX.

---

## 7. Hướng dẫn chạy Server/Client theo thứ tự

### Bước 1: Bật MySQL

Đảm bảo MySQL đang chạy và đã có database:

```sql
CREATE DATABASE IF NOT EXISTS auction_db;
```

Đảm bảo file sau đã cấu hình đúng tài khoản MySQL:

```text
auction-server/src/main/resources/db.properties
```

---

### Bước 2: Chạy server trước

Mở terminal tại thư mục gốc project và chạy:

```bash
java -jar auction-server/target/auction-server.jar
```

Server mặc định chạy ở port:

```text
8080
```

Khi chạy server, cần giữ nguyên terminal server và không tắt.

---

### Bước 3: Chạy client sau

Mở terminal thứ hai tại thư mục gốc project và chạy:

```bash
java -jar auction-client/target/auction-client.jar
```

Nếu client yêu cầu nhập địa chỉ server, nhập:

```text
127.0.0.1
```

Nếu server chạy trên máy khác, nhập địa chỉ IP của máy đang chạy server.

---

### Bước 4: Chạy nhiều client cùng lúc

Có thể mở nhiều terminal và chạy nhiều client:

```bash
java -jar auction-client/target/auction-client.jar
```

Ví dụ:

```text
Terminal 1: chạy server
Terminal 2: chạy client user A
Terminal 3: chạy client user B
Terminal 4: chạy client admin
```

Cách này dùng để kiểm thử chức năng nhiều người dùng cùng tham gia đấu giá.

---

## 8. Danh sách chức năng đã hoàn thành

### 8.1. Chức năng tài khoản

- Đăng ký tài khoản.
- Đăng nhập.
- Quên mật khẩu.
- Đăng xuất.
- Cập nhật thông tin cá nhân.
- Cập nhật ảnh đại diện.
- Nạp tiền vào tài khoản.
- Quản lý trạng thái tài khoản.

### 8.2. Chức năng đấu giá

- Xem danh sách sản phẩm đấu giá.
- Xem chi tiết sản phẩm.
- Thêm sản phẩm/lô đấu giá.
- Cập nhật sản phẩm đang chờ duyệt.
- Hủy sản phẩm của người bán.
- Đặt giá sản phẩm.
- Theo dõi giá hiện tại.
- Cập nhật giá theo thời gian thực.
- Xử lý kết thúc phiên đấu giá.

### 8.3. Chức năng người bán

- Đăng sản phẩm đấu giá.
- Xem danh sách sản phẩm của mình.
- Cập nhật thông tin sản phẩm đang chờ duyệt.
- Hủy sản phẩm.
- Nhận thông báo khi có người đặt giá.

### 8.4. Chức năng người mua

- Xem sản phẩm đang đấu giá.
- Đặt giá sản phẩm.
- Theo dõi sản phẩm yêu thích.
- Xem lịch sử giao dịch.
- Nhận thông báo khi bị người khác đặt giá cao hơn.

### 8.5. Chức năng watchlist

- Thêm sản phẩm vào danh sách theo dõi.
- Xóa sản phẩm khỏi danh sách theo dõi.
- Hiển thị danh sách sản phẩm đang theo dõi.
- Cập nhật trạng thái theo dõi trên giao diện.

### 8.6. Chức năng đánh giá

- Gửi đánh giá.
- Xem danh sách đánh giá.
- Hiển thị điểm đánh giá trung bình.

### 8.7. Chức năng chat và bạn bè

- Chat giữa người dùng.
- Tìm kiếm người dùng.
- Gửi lời mời kết bạn.
- Chấp nhận lời mời kết bạn.
- Từ chối lời mời kết bạn.
- Hiển thị tin nhắn trên giao diện client.

### 8.8. Chức năng quản trị

- Quản lý người dùng.
- Khóa tài khoản.
- Mở khóa tài khoản.
- Theo dõi trạng thái người dùng.
- Quản lý dữ liệu hệ thống ở mức cơ bản.

### 8.9. Chức năng hệ thống

- Client và server chạy riêng.
- Server hỗ trợ nhiều client kết nối cùng lúc.
- Giao tiếp qua TCP Socket.
- Server xử lý request thông qua các handler riêng biệt.
- Kết nối MySQL thông qua connection pool.
- Tự động tạo/cập nhật bảng bằng migration.
- Đóng gói được thành file executable fat JAR / uber JAR.
- Chạy được bằng lệnh `java -jar`.

---

## 9. Quy trình kiểm thử nhanh

Có thể kiểm thử chương trình theo thứ tự sau:

```text
1. Bật MySQL.
2. Chạy server bằng file auction-server.jar.
3. Chạy client bằng file auction-client.jar.
4. Đăng ký tài khoản mới.
5. Đăng nhập.
6. Thêm sản phẩm đấu giá.
7. Mở client thứ hai.
8. Đăng nhập bằng tài khoản khác.
9. Đặt giá sản phẩm.
10. Kiểm tra client còn lại có nhận cập nhật giá không.
```

---

## 10. Ghi chú khi chạy chương trình

- Server phải chạy trước client.
- MySQL phải được bật trước khi chạy server.
- File `db.properties` phải cấu hình đúng tài khoản MySQL.
- Nếu sửa file trong `src/main/resources`, cần build lại project để file JAR nhận cấu hình mới.
- Nếu muốn test nhiều người dùng, có thể mở nhiều client cùng lúc.
- Nhánh nộp cuối cùng là nhánh `main`.
- Không commit thêm sau deadline theo yêu cầu của giảng viên.

11. Videodemo - Project : https://l.facebook.com/l.php?u=https%3A%2F%2Fdrive.google.com%2Ffile%2Fd%2F1f-rXYu2PapCGe3ON3zm6eOxHXkE3EIol%2Fview%3Fusp%3Ddrivesdk%26fbclid%3DIwZXh0bgNhZW0CMTAAYnJpZBExSGVrRkNpaWNDR05hU1pYRXNydGMGYXBwX2lkEDIyMjAzOTE3ODgyMDA4OTIAAR5kyk2TA-WRr_lAzOky55SFAk8dp2ht_-l1C8LgYeYkK1S1EPdCtEiCo3T0dw_aem_-2vilnAuoisbbHAD0DGmQA&h=AUB6irptr-G-KK8uETchsg_6eBkV-gXE1xlU6AAR9nY3TmMqC3SJ8Ethi2GpkUf2PZIFChxSBXbjCMoh8ahTOE5JetaSg-q_PY5qpR5EhpGLen8EbkpSXBOtZ6j6DXVA8Th4hg
