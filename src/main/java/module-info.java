module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.auction.client to javafx.fxml;
    exports com.auction.client;
}