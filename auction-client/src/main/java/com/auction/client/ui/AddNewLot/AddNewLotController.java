package com.auction.client.ui.AddNewLot;
import com.auction.client.ClientSession;
import com.auction.client.network.NetworkClient;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.Request;
import com.auction.shared.Response;
import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
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
private final AtomicLong uploadUiGen = new AtomicLong(0L);
private static AddNewLotController live;
private static final String CATEGORY_IN_BOX = "CATEGORY";
public static void resetWhenOpening() {
AddNewLotController c = live;
if (c != null) {
c.clearForm();
}
}
@FXML
public void initialize() {
live = this;
classifyComboBox.setPromptText(CATEGORY_IN_BOX);
classifyComboBox.setEditable(false);
classifyComboBox.getItems().addAll("Electronics", "Art", "Vehicle");
ComboBox<String> box = classifyComboBox;
box.setButtonCell(
new ListCell<>() {
@Override
protected void updateItem(String item, boolean empty) {
super.updateItem(item, empty);
if (item == null) {
setText(CATEGORY_IN_BOX);
} else {
setText(item);
}
}
});
}
@FXML
public void handlechoosepicture(ActionEvent e) {
FileChooser ans = new FileChooser();
File res = ans.showOpenDialog(null);
if (res != null) {
final long gen = uploadUiGen.incrementAndGet();
new Thread(() -> {
try {
HttpClient client = HttpClient.newHttpClient();
String boundary = "boundary123";
String head = "--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n";
String tail = "\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--" + boundary + "--\r\n";
byte[] headbytes = head.getBytes();
byte[] filebytes = Files.readAllBytes(res.toPath());
byte[] tailbytes = tail.getBytes();
byte[] body = new byte[headbytes.length + filebytes.length + tailbytes.length];
System.arraycopy(headbytes, 0, body, 0, headbytes.length);
System.arraycopy(filebytes, 0, body, headbytes.length, filebytes.length);
System.arraycopy(tailbytes, 0, body, headbytes.length + filebytes.length, tailbytes.length);
HttpRequest req = HttpRequest.newBuilder().uri(URI.create("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload")).header("Content-Type", "multipart/form-data; boundary=" + boundary).POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
HttpResponse<String> response = client.send(req, HttpResponse.BodyHandlers.ofString());
String responseBody = response.body();
if (!responseBody.contains("\"secure_url\"")) {
Platform.runLater(() -> {
if (gen != uploadUiGen.get()) return;
lblStatus.setText("upload fail");
});
return;
}
String url = responseBody.split("\"secure_url\":\"")[1].split("\"")[0];
if (url.contains(".webp")) url = url.replace(".webp", ".jpg");
lotimageurl = url;
String urlFinal = url;
Platform.runLater(() -> {
if (gen != uploadUiGen.get()) return;
lblStatus.setText("");
productImageView.setImage(new Image(urlFinal, true));
});
} catch (Exception ex) {
Platform.runLater(() -> {
if (gen != uploadUiGen.get()) return;
lblStatus.setText("upload fail");
});
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
String timenorm = normalizeEndTimeForServer(time);
if (timenorm == null) {
lblStatus.setText("invalid end time (dd/MM/yyyy HH:mm or dd/MM/yyyy)");
return;
}
new Thread(() -> {
try {
Map<String, String> data = new HashMap<>();
data.put("name", name);
data.put("startingprice", price);
data.put("description", desc);
data.put("endtime", timenorm);
data.put("category", cat);
data.put("sellerusername", ClientSession.getUsername());
data.put("imageurl", lotimageurl);
Request req = new Request(Request.addlot, data);
Response res = NetworkClient.getinstance().sendrequestandwait(req);
Platform.runLater(() -> {
if (res != null && Response.ok.equals(res.getstatus())) {
KhungController.returnFromAddLot(true);
clearForm();
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
KhungController.returnFromAddLot(false);
clearForm();
}
private void clearForm() {
uploadUiGen.incrementAndGet();
txtName.clear();
txtPrice.clear();
txtQuantity.clear();
txtEndTime.clear();
classifyComboBox.getSelectionModel().clearSelection();
classifyComboBox.setValue(null);
classifyComboBox.setPromptText("CATEGORY");
lblStatus.setText("");
lotimageurl = "";
java.net.URL hutao = getClass().getResource("/images/Hutao.png");
if (hutao != null) {
productImageView.setImage(new Image(hutao.toExternalForm(), false));
} else {
productImageView.setImage(null);
}
}
private String normalizeEndTimeForServer(String raw) {
if (raw == null || raw.isBlank()) return null;
try {
LocalDateTime.parse(raw, fmt);
return raw;
} catch (DateTimeParseException e) {
try {
return LocalDate.parse(raw, DateTimeFormatter.ofPattern("dd/MM/yyyy")).atTime(23, 59).format(fmt);
} catch (DateTimeParseException e2) {
return null;
}
}
}
}
