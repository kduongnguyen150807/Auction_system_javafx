module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;

    exports com.auction.client.app;
    opens com.auction.client.ui.SearchBar to javafx.fxml;
    opens com.auction.client.ui.TrangChu to javafx.fxml;
    opens com.auction.client.ui.Main to javafx.fxml;
    opens com.auction.client.ui.ItemCard to javafx.fxml;
    opens com.auction.client.ui.ItemInformation to javafx.fxml;
    opens com.auction.client.ui.BiddingForm;
}