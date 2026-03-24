module Auction.system.javafx {
    requires javafx.controls;
    requires javafx.fxml;

    // Thêm dòng này để cấp quyền sử dụng các thư viện Database (SQL)
    requires java.sql;

    // Cấp quyền cho JavaFX đọc file
    opens com.auction.client to javafx.fxml;
    opens com.auction.client.controller to javafx.fxml;

    exports com.auction.client;
}