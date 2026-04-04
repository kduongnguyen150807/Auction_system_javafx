package com.auction.client.ui.TransactionHistory;

import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.TransactionLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import java.util.List;

public class TransactionHistoryController {
    @FXML private TableView<TransactionLog> table;
    @FXML private TableColumn<TransactionLog, String> idcol;
    @FXML private TableColumn<TransactionLog, String> typecol;
    @FXML private TableColumn<TransactionLog, String> amountcol;
    @FXML private TableColumn<TransactionLog, String> itemcol;
    @FXML private TableColumn<TransactionLog, String> datecol;

    @FXML
    public void initialize() {
        try {
            idcol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(String.valueOf(res.getValue().getid())));
            typecol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(res.getValue().gettype()));
            amountcol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f$", res.getValue().getamount())));
            itemcol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(String.valueOf(res.getValue().getitemid())));
            datecol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(res.getValue().getcreatedat() != null ? res.getValue().getcreatedat().toString() : "N/A"));
            loaddata();
        } catch (Exception e) {}
    }

    private void loaddata() {
        if (ClientSession.getCurrentUser() == null) return;
        int res = ClientSession.getCurrentUser().getid();
        Request req = new Request("get_transactions", res);
        Response ans = NetworkClient.getinstance().sendrequestandwait(req);
        if (ans != null && Response.ok.equals(ans.getstatus())) {
            try {
                List<TransactionLog> res1 = (List<TransactionLog>) ans.getpayload();
                if (res1 != null) {
                    ObservableList<TransactionLog> ans1 = FXCollections.observableArrayList(res1);
                    table.setItems(ans1);
                }
            } catch (Exception e) {}
        }
    }
}