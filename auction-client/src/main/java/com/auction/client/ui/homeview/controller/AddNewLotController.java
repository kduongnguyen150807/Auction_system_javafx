package com.auction.client.ui.homeview.controller;

import com.auction.client.service.AuctionService;
import com.auction.client.store.userinformation.ClientSession;
import com.auction.client.ui.base.CanRefresh;
import com.auction.client.ui.component.YearMonthDayHourMinuteSecond;
import com.auction.client.ui.homeview.homeviewcomponent.RedOrBlueToolbar;
import com.auction.client.util.AlertUtil;
import com.auction.shared.AuctionType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class AddNewLotController implements CanRefresh {
  /* basic field */
  @FXML private ImageView productImageView;
  @FXML private TextField nameField;
  @FXML private TextField priceField;
  @FXML private TextField maxPriceField;
  @FXML private TextArea descriptionField;
  @FXML private YearMonthDayHourMinuteSecond startTimeChooser;
  @FXML private YearMonthDayHourMinuteSecond endTimeChooser;
  @FXML private ComboBox<String> classifyComboBox;

  /* Dutch type field */
  @FXML private VBox dutchExtrasVBox;
  @FXML private TextField txtDutchReservePrice;
  @FXML private TextField txtDutchDecrement;
  @FXML private TextField txtDutchIntervalMinutes;

  @FXML private RedOrBlueToolbar<AuctionType> auctionToggle;
  private final ToggleGroup auctionTypeGroup = new ToggleGroup();

  private final List<String> categories = List.of("VEHICLE", "ART", "ELECTRONICS");
  private final String DEFAULT_CATEGORY = "VEHICLE";
  private String lotimageurl;
  private final AtomicLong uploadUiGen = new AtomicLong(0L);

  @FXML
  public void initialize() {
    setUpUi();
    setToggleBar();
  }

  private void setUpUi() {
    startTimeChooser.setDefaultValues(0, 0, 0);
    endTimeChooser.setDefaultValues(23, 59, 59);
    classifyComboBox.getItems().setAll(categories);
  }

  private void setToggleBar() {
    auctionToggle.setUpToggleGroup(auctionTypeGroup);
    auctionToggle.setData("AUCTION TYPE", AuctionType.ENGLISH, AuctionType.DUTCH);
    auctionTypeGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue == null) {
        oldValue.setSelected(true);
        return;
      }
      filterAuctionType(newValue);
    });
  }

  @FXML
  private void handleChoosePicture() {
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
          Platform.runLater(() -> {
            if (gen == uploadUiGen.get()) AlertUtil.showErrorAlert("upload fail", "failed to upload");
          });
          return;
        }
        String url = responseBody.split("\"secure_url\":\"")[1].split("\"")[0];
        if (url.contains(".webp")) url = url.replace(".webp", ".jpg");
        lotimageurl = url;
        String finalUrl = url;
        Platform.runLater(() -> {
          if (gen == uploadUiGen.get()) {
            productImageView.setImage(new Image(finalUrl, true));
          }
        });
      } catch (Exception ex) {
        Platform.runLater(() -> {
          if (gen == uploadUiGen.get()) AlertUtil.showErrorAlert("upload fail", "failed to upload");
        });
      }
    }).start();
  }

  @FXML
  private void handleSubmit() {
    Map<String, String> lotForm = collectData();
    if (lotForm == null) return;

    String message = AuctionService.registerLot(lotForm);
    if (message != null) {
      AlertUtil.showInfoAlert("ADD NEW LOT", message);
    } else {
      AlertUtil.showInfoAlert("ADD NEW LOT", "Successfully added lot");
      clear();
    }
  }

  @FXML
  private void handleCancel() {
    clear();
  }

  @Override
  public void refreshData() {
    clear();
  }

  private Map<String, String> collectData() {
    Map<String, String> result = new HashMap<>();
    AuctionType auctionType = (AuctionType) auctionTypeGroup.getSelectedToggle().getUserData();

    /* basic value */
    String name = nameField.getText().trim();
    String price = priceField.getText().trim();
    String maxPrice = maxPriceField.getText().trim();
    String description = descriptionField.getText().trim();
    if (name.isEmpty() || price.isEmpty() || description.isEmpty()) {
      AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "Please fill all the fields");
      return null;
    }
    /* time */
    if (startTimeChooser.collectDataInLocalDateTime().isAfter(endTimeChooser.collectDataInLocalDateTime())) {
      AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "Start time can not be after end time");
      return null;
    }
    String startTime = startTimeChooser.collectData();
    String endTime = endTimeChooser.collectData();

    /* category */
    String category;
    String classify = classifyComboBox.getValue();
    if (classify == null || classify.isEmpty()) {
      category = DEFAULT_CATEGORY;
    } else {
      category = classify;
    }

    /* Dutch check */
    if (auctionType.equals(AuctionType.DUTCH)) {
      String dutchReservePrice = txtDutchReservePrice.getText().trim();
      String dutchDecrement = txtDutchDecrement.getText().trim();
      String dutchIntervalMinutes = txtDutchIntervalMinutes.getText().trim();
      if (dutchReservePrice.isEmpty() || dutchDecrement.isEmpty() || dutchIntervalMinutes.isEmpty()) {
        AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "Please fill all the fields");
        return null;
      }
      try {
        double reserve = Double.parseDouble(dutchReservePrice);
        double dec = Double.parseDouble(dutchDecrement);
        int iv = Integer.parseInt(dutchIntervalMinutes);
        double ceiling = Double.parseDouble(price);
        if (reserve < 0 || dec <= 0 || iv <= 0) {
          AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "INVALID VALUE");
          return null;
        }
        if (reserve >= ceiling) {
          AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "RESERVED VALUE MUST BE BELOW CEILING");
          return null;
        }

        result.put("dutchreserve", dutchReservePrice);
        result.put("dutchdecrement", dutchDecrement);
        result.put("dutchintervalminutes", dutchIntervalMinutes);
      } catch (NumberFormatException ex) {
        AlertUtil.showErrorAlert("ADD NEW LOT FAILED", "INVALID DUTCH NUMBERS");
      }
    }

    result.put("name", name);
    result.put("startingprice", price);
    result.put("maxprice", maxPrice);
    result.put("description", description);
    result.put("category", category);
    result.put("starttime", startTime);
    result.put("endtime", endTime);
    result.put("sellerusername", ClientSession.CURRENT_SESSION.getCurrentUser().getUsername());
    result.put("imageurl", lotimageurl);
    return result;
  }

  private void filterAuctionType(Toggle toggle) {
    if (toggle.getUserData().equals(AuctionType.ENGLISH)) {
      dutchExtrasVBox.setVisible(false);
      dutchExtrasVBox.setManaged(false);
    } else {
      dutchExtrasVBox.setVisible(true);
      dutchExtrasVBox.setManaged(true);
    }
  }

  private void clear() {
    productImageView.setImage(null);
    nameField.clear();
    priceField.clear();
    maxPriceField.clear();
    descriptionField.clear();

    txtDutchReservePrice.clear();
    txtDutchDecrement.clear();
    txtDutchIntervalMinutes.clear();

    classifyComboBox.getSelectionModel().clearSelection();
    startTimeChooser.resetToDefault();
    endTimeChooser.resetToDefault();

    filterAuctionType(auctionToggle.getRedToggle());
  }
}