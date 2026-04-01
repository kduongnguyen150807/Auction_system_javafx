package com.auction.client.ui.Main;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import com.auction.shared.User;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
public class AdminDashboardController {
@FXML private TableView<User> usertable;
@FXML private TableColumn<User, String> colusername;
@FXML private TableColumn<User, String> colemail;
@FXML private TableColumn<User, String> colrole;
@FXML private TableColumn<User, String> colstatus;
@FXML private Button btnban;
@FXML private Button btnunban;
private ObservableList<User> userlist = FXCollections.observableArrayList();
@FXML
public void initialize() {
colusername.setCellValueFactory(new PropertyValueFactory<>("username"));
colemail.setCellValueFactory(new PropertyValueFactory<>("email"));
colrole.setCellValueFactory(celldata -> new SimpleStringProperty(celldata.getValue().getrole().toString()));
colstatus.setCellValueFactory(celldata -> new SimpleStringProperty(celldata.getValue().islocked() ? "locked" : "active"));
usertable.setItems(userlist);
loadusers();
}
private void loadusers() {
Request req = new Request(Request.getallusers, null);
NetworkClient.getinstance().sendrequest(req);
Response res = NetworkClient.getinstance().receiveresponse();
if (res != null && Response.ok.equals(res.getstatus())) {
List<User> ans = (List<User>) res.getpayload();
if (ans != null) userlist.addAll(ans);
}
}
@FXML
private void handleban(ActionEvent event) {
User selecteduser = usertable.getSelectionModel().getSelectedItem();
if (selecteduser == null) return;
Request req = new Request(Request.lockuser, selecteduser.getusername());
NetworkClient.getinstance().sendrequest(req);
Response res = NetworkClient.getinstance().receiveresponse();
if (res != null && Response.ok.equals(res.getstatus())) {
selecteduser.setlocked(true);
usertable.refresh();
}
}
@FXML
private void handleunban(ActionEvent event) {
User selecteduser = usertable.getSelectionModel().getSelectedItem();
if (selecteduser == null) return;
Request req = new Request(Request.unlockuser, selecteduser.getusername());
NetworkClient.getinstance().sendrequest(req);
Response res = NetworkClient.getinstance().receiveresponse();
if (res != null && Response.ok.equals(res.getstatus())) {
selecteduser.setlocked(false);
usertable.refresh();
}
}
}
