package com.auction.client.ui.AddNewLot;

import com.auction.client.ClientSession;
import com.auction.client.service.LotSubmissionService;
import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class AddNewLotController {
  @FXML private ImageView productImageView;
  @FXML private Label lblStatus;
  @FXML private Label formTitleLabel;
  @FXML private Button confirmSubmitButton;
  @FXML private TextField txtName, txtPrice, txtMaxPrice;
  @FXML private TextField txtDutchReservePrice, txtDutchDecrement, txtDutchIntervalMinutes;
  @FXML private TextArea txtQuantity;
  @FXML private VBox dutchExtrasVBox;
  @FXML private ToggleButton kindEnglishToggle;
  @FXML private ToggleButton kindDutchToggle;
  @FXML private DatePicker startDatePicker, endDatePicker;
  @FXML private ComboBox<Integer> startHourCombo, startMinuteCombo, startSecondCombo;
  @FXML private ComboBox<Integer> endHourCombo, endMinuteCombo, endSecondCombo;
  @FXML private ComboBox<String> classifyComboBox;

  private String lotimageurl = "";
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
  private final AtomicLong uploadUiGen = new AtomicLong(0L);
  private static AddNewLotController live;
  private static final String DEFAULT_CATEGORY = "Vehicle";
  private final LotSubmissionService lotSubmissionService = new LotSubmissionService();
  private Integer editingItemId;

  private ToggleGroup auctionKindGroup;

  public static void resetWhenOpening() {
    if (live != null) live.clearForm();
  }

  public void openForEdit(Item item) {
    if (item == null) {
      return;
    }
    resetFormFieldsKeepGen();
    editingItemId = item.getId();
    applyEditModeTitles(item);
    populateFromExistingItem(item);
    lblStatus.setText("");
  }

  private void resetFormFieldsKeepGen() {
    uploadUiGen.incrementAndGet();
    txtName.clear();
    txtPrice.clear();
    txtMaxPrice.clear();
    if (txtDutchReservePrice != null) txtDutchReservePrice.clear();
    if (txtDutchDecrement != null) txtDutchDecrement.clear();
    if (txtDutchIntervalMinutes != null) txtDutchIntervalMinutes.clear();
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
    if (kindEnglishToggle != null) kindEnglishToggle.setSelected(true);
    refreshAuctionKindPanels();
    java.net.URL hutao = getClass().getResource("/images/Hutao.png");
    productImageView.setImage(hutao != null ? new Image(hutao.toExternalForm(), false) : null);
  }

  private void populateFromExistingItem(Item item) {
    txtName.setText(item.getName() != null ? item.getName() : "");
    txtQuantity.setText(item.getDescription() != null ? item.getDescription() : "");
    txtPrice.setText(Double.toString(item.getStartingPrice()));
    AuctionType type = item.getAuctionType();
    if (kindDutchToggle != null && type == AuctionType.DUTCH) {
      kindDutchToggle.setSelected(true);
      if (txtDutchReservePrice != null)
        txtDutchReservePrice.setText(String.valueOf(item.getDutchReservePrice()));
      if (txtDutchDecrement != null)
        txtDutchDecrement.setText(String.valueOf(item.getDutchTickAmount()));
      if (txtDutchIntervalMinutes != null)
        txtDutchIntervalMinutes.setText(String.valueOf(item.getDutchTickIntervalMinutes()));
    } else if (kindEnglishToggle != null) {
      kindEnglishToggle.setSelected(true);
      double mx = item.getMaxPrice();
      txtMaxPrice.setText(mx > 0 ? Double.toString(mx) : "");
    }
    refreshAuctionKindPanels();

    LocalDateTime st = item.getStartTime();
    if (st != null) {
      startDatePicker.setValue(st.toLocalDate());
      startHourCombo.setValue(st.getHour());
      startMinuteCombo.setValue(st.getMinute());
      startSecondCombo.setValue(st.getSecond());
    }
    LocalDateTime et = item.getEndTime();
    if (et != null) {
      endDatePicker.setValue(et.toLocalDate());
      endHourCombo.setValue(et.getHour());
      endMinuteCombo.setValue(et.getMinute());
      endSecondCombo.setValue(et.getSecond());
    }

    String cat = item.getCategory();
    if (cat != null && !cat.isBlank()) {
      if (!classifyComboBox.getItems().contains(cat)) {
        classifyComboBox.getItems().add(cat);
      }
      classifyComboBox.setValue(cat);
    }

    lotimageurl = item.getImageUrl() != null ? item.getImageUrl() : "";
    if (!lotimageurl.isBlank()) {
      productImageView.setImage(new Image(lotimageurl, true));
    }
  }

  private void applyAddModeTitles() {
    if (formTitleLabel != null) {
      formTitleLabel.setText("ADD NEW ITEM");
    }
    if (confirmSubmitButton != null) {
      confirmSubmitButton.setText("CONFIRM UPLOAD");
    }
  }

  private void applyEditModeTitles(Item item) {
    boolean openBeforeStart =
        item != null
            && item.getStatus() == ItemStatus.OPEN
            && item.getStartTime() != null
            && item.getStartTime().isAfter(LocalDateTime.now());
    if (formTitleLabel != null) {
      formTitleLabel.setText(openBeforeStart ? "EDIT (BEFORE START)" : "EDIT PENDING ITEM");
    }
    if (confirmSubmitButton != null) {
      confirmSubmitButton.setText("SAVE CHANGES");
    }
  }

  @FXML
  public void initialize() {
    live = this;
    classifyComboBox.setPromptText("CATEGORY");
    classifyComboBox.setEditable(false);
    classifyComboBox.getItems().addAll("Electronics", "Art", "Vehicle");
    classifyComboBox.setButtonCell(new ListCell<>() {
      @Override protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty); setText(item == null ? "CATEGORY" : item);
      }
    });
    for (int i = 0; i < 24; i++) { startHourCombo.getItems().add(i); endHourCombo.getItems().add(i); }
    for (int i = 0; i < 60; i++) {
      startMinuteCombo.getItems().add(i); startSecondCombo.getItems().add(i);
      endMinuteCombo.getItems().add(i); endSecondCombo.getItems().add(i);
    }
    startHourCombo.setValue(0); startMinuteCombo.setValue(0); startSecondCombo.setValue(0);
    endHourCombo.setValue(23); endMinuteCombo.setValue(59); endSecondCombo.setValue(0);
    startDatePicker.setValue(LocalDate.now());
    endDatePicker.setValue(LocalDate.now().plusDays(1));

    if (kindEnglishToggle != null && kindDutchToggle != null) {
      auctionKindGroup = new ToggleGroup();
      kindEnglishToggle.setToggleGroup(auctionKindGroup);
      kindDutchToggle.setToggleGroup(auctionKindGroup);
      kindEnglishToggle.setSelected(true);
      auctionKindGroup
          .selectedToggleProperty()
          .addListener((obs, oldToggle, newToggle) -> refreshAuctionKindPanels());
      refreshAuctionKindPanels();
    }
    applyAddModeTitles();
  }

  private void refreshAuctionKindPanels() {
    if (kindEnglishToggle == null || dutchExtrasVBox == null || txtMaxPrice == null || txtPrice == null) {
      return;
    }
    boolean english = kindEnglishToggle.isSelected();
    txtMaxPrice.setVisible(english);
    txtMaxPrice.setManaged(english);
    txtPrice.setPromptText(english ? "START PRICE ($)" : "STARTING PRICE ($)");
    dutchExtrasVBox.setVisible(!english);
    dutchExtrasVBox.setManaged(!english);
  }

  @FXML
  public void handleChoosePicture(ActionEvent e) {
    File file = new FileChooser().showOpenDialog(null);
    if (file == null) return;
    final long gen = uploadUiGen.incrementAndGet();
    new Thread(() -> {
      try {
        String boundary = "boundary123";
        byte[] head = ("--" + boundary + "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"item.png\"\r\n\r\n").getBytes();
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        byte[] tail = ("\r\n--" + boundary + "\r\nContent-Disposition: form-data; name=\"upload_preset\"\r\n\r\nupload_def\r\n--" + boundary + "--\r\n").getBytes();
        byte[] body = new byte[head.length + fileBytes.length + tail.length];
        System.arraycopy(head, 0, body, 0, head.length);
        System.arraycopy(fileBytes, 0, body, head.length, fileBytes.length);
        System.arraycopy(tail, 0, body, head.length + fileBytes.length, tail.length);
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create("https://api.cloudinary.com/v1_1/khanhdn-tk/image/upload"))
            .header("Content-Type", "multipart/form-data; boundary=" + boundary)
            .POST(HttpRequest.BodyPublishers.ofByteArray(body)).build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(req, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();
        if (!responseBody.contains("\"secure_url\"")) {
          Platform.runLater(() -> { if (gen == uploadUiGen.get()) lblStatus.setText("upload fail"); });
          return;
        }
        String url = responseBody.split("\"secure_url\":\"")[1].split("\"")[0];
        if (url.contains(".webp")) url = url.replace(".webp", ".jpg");
        lotimageurl = url;
        String finalUrl = url;
        Platform.runLater(() -> { if (gen == uploadUiGen.get()) { lblStatus.setText(""); productImageView.setImage(new Image(finalUrl, true)); } });
      } catch (Exception ex) {
        Platform.runLater(() -> { if (gen == uploadUiGen.get()) lblStatus.setText("upload fail"); });
      }
    }).start();
  }

  @FXML
  public void handleSubmit(ActionEvent e) {
    String name = txtName.getText().trim(), price = txtPrice.getText().trim();
    String maxPriceText = txtMaxPrice.getText().trim(), desc = txtQuantity.getText().trim();
    String catRaw = classifyComboBox.getValue();
    String category = catRaw == null || catRaw.isBlank() ? DEFAULT_CATEGORY : catRaw;
    if (desc.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Thiếu mô tả", "Vui lòng nhập mô tả cho sản phẩm."); return; }
    if (name.isEmpty() || price.isEmpty() || anyNull(startDatePicker.getValue(), startHourCombo.getValue(),
        startMinuteCombo.getValue(), startSecondCombo.getValue(), endDatePicker.getValue(),
        endHourCombo.getValue(), endMinuteCombo.getValue(), endSecondCombo.getValue())) {
      lblStatus.setText("fill all fields"); return;
    }
    String startNorm = buildDateTime(startDatePicker.getValue(), startHourCombo.getValue(), startMinuteCombo.getValue(), startSecondCombo.getValue());
    String endNorm = buildDateTime(endDatePicker.getValue(), endHourCombo.getValue(), endMinuteCombo.getValue(), endSecondCombo.getValue());
    if (startNorm == null || endNorm == null) { lblStatus.setText("invalid start/end time"); return; }
    LocalDateTime startDt = parseDateTime(startNorm), endDt = parseDateTime(endNorm);
    if (startDt == null || endDt == null || !endDt.isAfter(startDt)) {
      showAlert(Alert.AlertType.WARNING, "Invalid time range", "End time must be after start time."); return;
    }
    boolean dutch = kindDutchToggle != null && kindDutchToggle.isSelected();
    if (dutch) {
      if (txtDutchReservePrice == null || txtDutchDecrement == null || txtDutchIntervalMinutes == null) {
        lblStatus.setText("missing dutch controls");
        return;
      }
      String r = txtDutchReservePrice.getText().trim();
      String step = txtDutchDecrement.getText().trim();
      String mins = txtDutchIntervalMinutes.getText().trim();
      if (r.isEmpty() || step.isEmpty() || mins.isEmpty()) {
        lblStatus.setText("complete Dutch reserve, step, interval");
        return;
      }
      try {
        double reserve = Double.parseDouble(r);
        double dec = Double.parseDouble(step);
        int iv = Integer.parseInt(mins);
        double ceiling = Double.parseDouble(price);
        if (reserve < 0 || dec <= 0 || iv <= 0) {
          lblStatus.setText("invalid Dutch numeric fields"); return;
        }
        if (reserve >= ceiling) {
          lblStatus.setText("reserve must be below starting price"); return;
        }
      } catch (NumberFormatException ex) {
        lblStatus.setText("invalid Dutch numbers"); return;
      }
    }
    String finalStart = startNorm, finalEnd = endNorm;
    Integer editCopy = editingItemId;
    new Thread(() -> {
      try {
        Map<String, String> data = new HashMap<>();
        data.put("name", name);
        data.put("startingprice", price);
        boolean dutchAuction = kindDutchToggle != null && kindDutchToggle.isSelected();
        data.put("auctiontype", dutchAuction ? "DUTCH" : "ENGLISH");
        if (dutchAuction) {
          data.put("maxprice", "0");
          data.put(
              "dutchreserve",
              txtDutchReservePrice != null ? txtDutchReservePrice.getText().trim() : "");
          data.put("dutchdecrement", txtDutchDecrement != null ? txtDutchDecrement.getText().trim() : "");
          data.put(
              "dutchintervalmins",
              txtDutchIntervalMinutes != null ? txtDutchIntervalMinutes.getText().trim() : "");
        } else {
          data.put("maxprice", maxPriceText.isEmpty() ? "0" : maxPriceText);
        }
        data.put("description", desc);
        data.put("starttime", finalStart);
        data.put("endtime", finalEnd);
        data.put("category", category);
        data.put("imageurl", lotimageurl);

        if (editCopy != null) {
          data.put("itemid", String.valueOf(editCopy));
          Response res = lotSubmissionService.updatePendingLot(data);
          Platform.runLater(
              () -> {
                if (res != null && Response.OK.equals(res.getStatus())) {
                  showAlert(
                      Alert.AlertType.INFORMATION,
                      "Đã lưu",
                      "Đã cập nhật sản phẩm chờ duyệt.");
                  KhungController.returnFromLotEditor(true);
                  clearForm();
                } else lblStatus.setText(res != null ? res.getMessage() : "fail");
              });
          return;
        }

        data.put("sellerusername", ClientSession.getUsername());
        Response res = lotSubmissionService.submitLot(data);
        Platform.runLater(() -> {
          if (res != null && Response.OK.equals(res.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Item Submitted", "Your item has been submitted and is pending admin approval.");
            KhungController.returnFromAddLot(true); clearForm();
          } else lblStatus.setText(res != null ? res.getMessage() : "fail");
        });
      } catch (Exception ex) {
        Platform.runLater(() -> lblStatus.setText("error"));
      }
    }).start();
  }

  @FXML
  public void handleCancel(ActionEvent e) {
    boolean editing = editingItemId != null;
    if (editing) {
      KhungController.returnFromLotEditor(false);
    } else {
      KhungController.returnFromAddLot(false);
    }
    clearForm();
  }

  private boolean anyNull(Object... values) {
    for (Object v : values) if (v == null) return true;
    return false;
  }

  private void showAlert(Alert.AlertType type, String title, String msg) {
    Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
  }

  public void clearForm() {
    resetFormFieldsKeepGen();
    editingItemId = null;
    applyAddModeTitles();
  }

  private String buildDateTime(LocalDate date, Integer h, Integer m, Integer s) {
    if (date == null || h == null || m == null || s == null) return null;
    try { return LocalDateTime.of(date, LocalTime.of(h, m, s)).format(FMT); }
    catch (DateTimeParseException e) { return null; }
  }

  private LocalDateTime parseDateTime(String value) {
    if (value == null || value.isBlank()) return null;
    try { return LocalDateTime.parse(value, FMT); }
    catch (DateTimeParseException e) { return null; }
  }
}
