# 🔨 Hệ Thống Đấu Giá Trực Tuyến (Online Auction System)

Dự án phát triển hệ thống đấu giá trực tuyến thuộc bài tập lớn môn Lập trình nâng cao. Hệ thống cho phép nhiều người dùng tham gia cạnh tranh giá realtime để mua sản phẩm trong một khoảng thời gian xác định.

Dự án được phát triển bởi nhóm 4 thành viên, áp dụng kiến trúc Client-Server phân tầng, thiết kế hướng đối tượng (OOP) và các Design Pattern chuẩn mực.

## 🏗 Kiến Trúc Hệ Thống (Architecture)

Hệ thống được chia làm 3 module chính để phân tách rõ ràng giao diện và logic xử lý:

1. **`auction-shared`**: Chứa các Entity (User, Item, BidTransaction) dùng chung cho cả Client và Server.

2. **`auction-server`**: Áp dụng mô hình MVC (Controller → Model → DAO/Database), là nơi duy nhất truy cập cơ sở dữ liệu. Xử lý các nghiệp vụ cốt lõi, concurrent bidding và quản lý kết nối.

3. **`auction-client`**: Áp dụng mô hình MVC với JavaFX và FXML. Xử lý giao diện người dùng và giao tiếp với Server qua mạng.

## 🚀 Các Tính Năng Nổi Bật

Chức Năng Cốt Lõi

- **Quản lý đa vai trò:** Hỗ trợ Admin, Seller (đăng sản phẩm) và Bidder (tham gia đấu giá).

- **Quản lý phiên đấu giá:** Tự động mở/đóng phiên theo thời gian thực, cập nhật người dẫn đầu và xác định người thắng cuộc.

- **Bảo vệ toàn vẹn dữ liệu:** Xử lý triệt để các lỗi ngoại lệ như đặt giá thấp hơn giá hiện tại hoặc đấu giá khi phiên đã đóng.

Kỹ Thuật Nâng Cao

- **Realtime Bidding (Observer Pattern):** Cập nhật giá đấu ngay lập tức cho toàn bộ client mà không cần polling.

- **Concurrent Bidding:** Xử lý đấu giá đồng thời an toàn, ngăn chặn tình trạng lost update, rollback giá hay hai người cùng thắng.

- **Auto-Bidding & Anti-Sniping:** Hỗ trợ tự động trả giá với `maxBid` và tự động gia hạn thời gian nếu có biến động ở những giây cuối.

- **Realtime Price Curve:** Trực quan hóa lịch sử đấu giá bằng biểu đồ đường cập nhật theo thời gian thực.

## 🛠 Công Nghệ Sử Dụng

- **Ngôn ngữ:** Java

- **Giao diện (GUI):** JavaFX

- **Database:** MySQL / PostgreSQL

- **Giao tiếp mạng:** Socket / REST API

- **Build Tool:** Maven / Gradle

- **Testing & CI/CD:** JUnit, GitHub Actions

## 📝 Quy Định Làm Việc Nhóm (Dành cho thành viên)

Để đảm bảo tiến độ và tránh conflict mã nguồn, toàn bộ thành viên tuân thủ các quy tắc sau:

1. **Không push trực tiếp lên nhánh `main`.** Mọi tính năng mới phải được phát triển trên nhánh riêng (ví dụ: `feat/login`, `fix/bid-error`) và tạo Pull Request (PR) để review. (có thể push lên test để check)

2. **Quy tắc Commit:** Sử dụng Conventional Commits. Ví dụ:
   
   - `feat: ...` (Thêm tính năng mới)
   
   - `fix: ...` (Sửa lỗi)
   
   - `refactor: ...` (Tối ưu code)

3. **Trách nhiệm chéo:** Frontend (JavaFX) có thể sử dụng Mock Data trong lúc chờ Backend hoàn thiện API. Backend phải viết Unit Test (JUnit) cho các logic quan trọng trước khi tích hợp. Bất kỳ thành viên nào không hiểu phần code của mình, cả nhóm sẽ bị 0 điểm.

## ⚙️ Hướng Dẫn Cài Đặt & Chạy (Setup Instructions)

*(Phần này điền lệnh chạy maven/gradle để build project sau khi nhóm đã chốt xong công cụ)*