---

## 🚀 Module Auction Server - Bộ Não Hệ Thống Đấu Giá

Module này đóng vai trò là trung tâm xử lý dữ liệu (Backend), áp dụng kiến trúc **Client-Server** và mô hình **MVC (Controller → Model → DAO/Database)**. Đây là nơi duy nhất trong hệ thống có quyền truy cập trực tiếp vào cơ sở dữ liệu để đảm bảo tính an toàn và toàn vẹn dữ liệu.

### 📂 Cấu trúc thư mục (Directory Structure)

| **Gói (Package)** | **Vai trò & Nhiệm vụ**                                                                |
| ----------------- | ------------------------------------------------------------------------------------- |
| `app`             | Khởi chạy Server (Main), thiết lập cổng Socket lắng nghe Client.                      |
| `network`         | Quản lý kết nối đa luồng (`ClientHandler`), giải mã `Request` và đóng gói `Response`. |
| `dao`             | Chọc trực tiếp vào Database (MySQL/PostgreSQL) để thực hiện CRUD.                     |
| `service`         | Xử lý logic nghiệp vụ: Đấu giá đồng thời, Anti-sniping, và quản lý phiên.             |
| `factory`         | Áp dụng **Factory Method Pattern** để khởi tạo các loại sản phẩm.                     |
