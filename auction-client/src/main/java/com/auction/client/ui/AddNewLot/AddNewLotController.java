package com.auction.client.ui.AddNewLot;
import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
public class AddNewLotController {
@FXML private ImageView productImageView;
@FXML private Label lblStatus;
@FXML private TextField txtName;
@FXML private TextField txtPrice;
@FXML private TextArea txtQuantity;
@FXML private TextField txtEndTime;
@FXML private ComboBox<String> classifyComboBox;
private String lotimageurl = "";
private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
@FXML
public void initialize() {
classifyComboBox.getItems().addAll("Electronics", "Art", "Vehicle");
}
@FXML
public void handlechoosepicture(ActionEvent e) {
FileChooser ans = new FileChooser();
File res = ans.showOpenDialog(null);
if (res != null) {
new Thread(() -> {
try {
HttpClient client = HttpClient.newHttpClient();
String boundary = "boundary123";
String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n";
String tail = "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--" + boundary + "--\r\n";
byte[] filebytes = Files.readAllBytes(res.toPath());
byte[] body = new byte[head.getBytes().length + filebytes.length + tail.getBytes().length];
System.arraycopy(head.getBytes(), 0, body, 0, head.getBytes().length);
System.arraycopy(filebytes, 0, body, head.getBytes().length, filebytes.length);
System.arraycopy(tail.getBytes(), 0, body, head.getBytes().length + filebytes.length, tail.getBytes().length);
HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload")).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
String url = response.body().split("\"secure_url\":\"")[1].split("\"")[0];
lotimageurl = url.replace(".webp", ".jpg");
Platform.runLater(() -> productImageView.setImage(new Image(lotimageurl, true)));
} catch (Exception ex) {
Platform.runLater(() -> lblStatus.setText("upload fail"));
}
}).start();
}
}
@FXML
public void handlesubmit(ActionEvent e) {
String name = txtName.getText().trim();
String price = txtPrice.getText().trim();
String desc = txtQuantity.getText().trim();
String time = txtEndTime.getText().trim();
String cat = classifyComboBox.getValue();
if (name.isEmpty() || price.isEmpty() || desc.isEmpty() || time.isEmpty() || cat == null) {
lblStatus.setText("fill all fields");
return;
}
new Thread(() -> {
try {
Map<String, String> data = new HashMap<>();
data.put("name", name);
data.put("startingprice", price);
data.put("description", desc);
data.put("endtime", time);
data.put("category", cat);
data.put("sellerusername", ClientSession.getUsername());
data.put("imageurl", lotimageurl);
Request req = new Request(Request.addlot, data);
NetworkClient.getinstance().sendrequest(req);
Response res = NetworkClient.getinstance().receiveresponse();
Platform.runLater(() -> {
if (res != null && Response.ok.equals(res.getstatus())) {
lblStatus.setText("success");
handlecancel(null);
} else {
lblStatus.setText(res != null ? res.getmessage() : "fail");
}
});
} catch (Exception ex) {
Platform.runLater(() -> lblStatus.setText("error"));
}
}).start();
}
@FXML
public void handlecancel(ActionEvent e) {
txtName.clear();
txtPrice.clear();
txtQuantity.clear();
txtEndTime.clear();
classifyComboBox.setValue(null);
lblStatus.setText("");
lotimageurl = "";
productImageView.setImage(new Image(getClass().getResource("/images/Hutao.png").toExternalForm(), true));
}
}
