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
    @FXML private TableColumn<TransactionLog, Integer> idcol;
    @FXML private TableColumn<TransactionLog, String> typecol;
    @FXML private TableColumn<TransactionLog, Double> amountcol;
    @FXML private TableColumn<TransactionLog, Integer> itemcol;
    @FXML private TableColumn<TransactionLog, java.time.LocalDateTime> datecol;

    @FXML
    public void initialize() {
        idcol.setCellValueFactory(res -> new javafx.beans.property.SimpleObjectProperty<>(res.getValue().getid()));
        typecol.setCellValueFactory(res -> new javafx.beans.property.SimpleStringProperty(res.getValue().gettype()));
        amountcol.setCellValueFactory(res -> new javafx.beans.property.SimpleObjectProperty<>(res.getValue().getamount()));
        itemcol.setCellValueFactory(res -> new javafx.beans.property.SimpleObjectProperty<>(res.getValue().getitemid()));
        datecol.setCellValueFactory(res -> new javafx.beans.property.SimpleObjectProperty<>(res.getValue().getcreatedat()));
        loaddata();
    }

    private void loaddata() {
        if (ClientSession.getCurrentUser() == null) return;
        int res = ClientSession.getCurrentUser().getid();
        Request req = new Request("get_transactions", res);
        Response ans = NetworkClient.getinstance().sendrequestandwait(req);
        if (ans != null && Response.ok.equals(ans.getstatus())) {
            List<TransactionLog> res1 = (List<TransactionLog>) ans.getpayload();
            ObservableList<TransactionLog> ans1 = FXCollections.observableArrayList(res1);
            table.setItems(ans1);
        }
    }
}