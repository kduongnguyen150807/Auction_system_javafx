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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class AddNewLotController {
    @FXML private ImageView productImageView;
    @FXML private Label lblStatus;
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtMaxPrice;
    @FXML private TextArea txtQuantity;
    @FXML private DatePicker startDatePicker;
    @FXML private ComboBox<Integer> startHourCombo;
    @FXML private ComboBox<Integer> startMinuteCombo;
    @FXML private ComboBox<Integer> startSecondCombo;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<Integer> endHourCombo;
    @FXML private ComboBox<Integer> endMinuteCombo;
    @FXML private ComboBox<Integer> endSecondCombo;
    @FXML private ComboBox<String> classifyComboBox;

    private String lotimageurl = "";
    private static final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private final AtomicLong uploadUiGen = new AtomicLong(0L);
    private static AddNewLotController live;
    private static final String CATEGORY_IN_BOX = "CATEGORY";
    private static final String DEFAULT_CATEGORY = "Vehicle";

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
        for (int i = 0; i < 24; i++) endHourCombo.getItems().add(i);
        for (int i = 0; i < 60; i++) {
            startMinuteCombo.getItems().add(i);
            startSecondCombo.getItems().add(i);
            endMinuteCombo.getItems().add(i);
            endSecondCombo.getItems().add(i);
        }
        for (int i = 0; i < 24; i++) startHourCombo.getItems().add(i);
        startHourCombo.setValue(0);
        startMinuteCombo.setValue(0);
        startSecondCombo.setValue(0);
        endHourCombo.setValue(23);
        endMinuteCombo.setValue(59);
        endSecondCombo.setValue(0);
        startDatePicker.setValue(LocalDate.now());
        endDatePicker.setValue(LocalDate.now().plusDays(1));
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
        String maxp = txtMaxPrice.getText().trim();
        String desc = txtQuantity.getText().trim();
        String catRaw = classifyComboBox.getValue();
        final String categoryForSubmit = (catRaw == null || catRaw.isBlank()) ? DEFAULT_CATEGORY : catRaw;
        if (desc.isEmpty()) {
            Alert needDesc = new Alert(Alert.AlertType.WARNING);
            needDesc.setTitle("Thiếu mô tả");
            needDesc.setHeaderText(null);
            needDesc.setContentText("Vui lòng nhập mô tả (description) cho sản phẩm.");
            needDesc.showAndWait();
            return;
        }
        if (name.isEmpty() || price.isEmpty() || startDatePicker.getValue() == null || startHourCombo.getValue() == null || startMinuteCombo.getValue() == null || startSecondCombo.getValue() == null || endDatePicker.getValue() == null || endHourCombo.getValue() == null || endMinuteCombo.getValue() == null || endSecondCombo.getValue() == null) {
            lblStatus.setText("fill all fields");
            return;
        }
        String startNorm = normalizeDateTimeForServer(startDatePicker.getValue(), startHourCombo.getValue(), startMinuteCombo.getValue(), startSecondCombo.getValue());
        String endNorm = normalizeDateTimeForServer(endDatePicker.getValue(), endHourCombo.getValue(), endMinuteCombo.getValue(), endSecondCombo.getValue());
        if (startNorm == null || endNorm == null) {
            lblStatus.setText("invalid start/end time");
            return;
        }
        LocalDateTime startDt = parseClientDateTime(startNorm);
        LocalDateTime endDt = parseClientDateTime(endNorm);
        if (startDt == null || endDt == null || !endDt.isAfter(startDt)) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Invalid time range");
            a.setHeaderText(null);
            a.setContentText("End time must be after start time.");
            a.showAndWait();
            return;
        }
        new Thread(() -> {
            try {
                Map<String, String> data = new HashMap<>();
                data.put("name", name);
                data.put("startingprice", price);
                data.put("maxprice", maxp.isEmpty() ? "0" : maxp);
                data.put("description", desc);
                data.put("starttime", startNorm);
                data.put("endtime", endNorm);
                data.put("category", categoryForSubmit);
                data.put("sellerusername", ClientSession.getUsername());
                data.put("imageurl", lotimageurl);
                Request req = new Request(Request.addlot, data);
                Response res = NetworkClient.getinstance().sendrequestandwait(req);
                Platform.runLater(() -> {
                    if (res != null && Response.ok.equals(res.getstatus())) {
                        Alert ans = new Alert(Alert.AlertType.INFORMATION);
                        ans.setTitle("Item Submitted");
                        ans.setHeaderText(null);
                        ans.setContentText("Your item has been submitted and is pending admin approval.");
                        ans.showAndWait();
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
        txtMaxPrice.clear();
        txtQuantity.clear();
        startDatePicker.setValue(LocalDate.now());
        startHourCombo.setValue(0);
        startMinuteCombo.setValue(0);
        startSecondCombo.setValue(0);
        endDatePicker.setValue(LocalDate.now().plusDays(1));
        endHourCombo.setValue(23);
        endMinuteCombo.setValue(59);
        endSecondCombo.setValue(0);
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

    private String normalizeDateTimeForServer(LocalDate date, Integer hour, Integer minute, Integer second) {
        if (date == null || hour == null || minute == null || second == null) return null;
        try {
            LocalDateTime dt = LocalDateTime.of(date, LocalTime.of(hour, minute, second));
            return dt.format(fmt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseClientDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value, fmt);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}