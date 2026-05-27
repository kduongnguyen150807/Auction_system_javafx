package com.auction.client.ui.AddNewLot;

import com.auction.client.ui.Main.KhungController;
import com.auction.shared.AuctionType;
import com.auction.shared.Item;
import com.auction.shared.ItemStatus;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class AddNewLotController {
  private static final String DEFAULT_CATEGORY = "Vehicle";

  @FXML private ImageView productImageView;
  @FXML private Label lblStatus;
  @FXML private Label formTitleLabel;
  @FXML private Button confirmSubmitButton;
  @FXML private TextField txtName, txtPrice, txtMaxPrice;
  @FXML private TextField txtDutchReservePrice, txtDutchDecrement, txtDutchIntervalMinutes;
  @FXML private Label lblDutchSchedulePreview;
  @FXML private TextArea txtQuantity;
  @FXML private VBox dutchExtrasVBox;
  @FXML private ToggleButton kindEnglishToggle;
  @FXML private ToggleButton kindDutchToggle;
  @FXML private DatePicker startDatePicker, endDatePicker;
  @FXML private ComboBox<Integer> startHourCombo, startMinuteCombo, startSecondCombo;
  @FXML private ComboBox<Integer> endHourCombo, endMinuteCombo, endSecondCombo;
  @FXML private ComboBox<String> classifyComboBox;

  private String lotimageurl = "";
  private final AtomicLong uploadUiGen = new AtomicLong(0L);
  private static AddNewLotController live;
  private final AddLotSubmissionCoordinator submissionCoordinator =
      new AddLotSubmissionCoordinator(new com.auction.client.service.LotSubmissionService());
  private Integer editingItemId;
  private ToggleGroup auctionKindGroup;

  public static void resetWhenOpening() {
    if (live != null) {
      live.clearForm();
    }
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
    refreshDutchSchedulePreview();
  }

  @FXML
  public void initialize() {
    live = this;
    AddLotFormInitializer.setupCategoryCombo(classifyComboBox);
    AddLotFormInitializer.setupTimeCombos(
        startHourCombo,
        startMinuteCombo,
        startSecondCombo,
        endHourCombo,
        endMinuteCombo,
        endSecondCombo,
        startDatePicker,
        endDatePicker);
    if (kindEnglishToggle != null && kindDutchToggle != null) {
      auctionKindGroup =
          AddLotFormInitializer.setupAuctionKindToggle(
              kindEnglishToggle, kindDutchToggle, this::refreshAuctionKindPanels);
      refreshAuctionKindPanels();
    }
    applyAddModeTitles();
    wireDutchPreviewListeners();
    if (txtDutchDecrement != null) {
      txtDutchDecrement.setEditable(false);
    }
  }

  @FXML
  public void handleChoosePicture(ActionEvent e) {
    File file = new FileChooser().showOpenDialog(null);
    if (file == null) {
      return;
    }
    AddLotImageUploader.uploadAsync(
        file,
        uploadUiGen,
        new AddLotImageUploader.Callbacks() {
          @Override
          public void onSuccess(String imageUrl, Image preview) {
            lotimageurl = imageUrl;
            lblStatus.setText("");
            productImageView.setImage(preview);
          }

          @Override
          public void onFailure() {
            lblStatus.setText("upload fail");
          }
        });
  }

  @FXML
  public void handleSubmit(ActionEvent e) {
    String name = txtName.getText().trim();
    String price = txtPrice.getText().trim();
    String maxPriceText = txtMaxPrice.getText().trim();
    String desc = txtQuantity.getText().trim();
    String catRaw = classifyComboBox.getValue();
    String category = catRaw == null || catRaw.isBlank() ? DEFAULT_CATEGORY : catRaw;

    if (desc.isEmpty()) {
      showAlert(Alert.AlertType.WARNING, "Thiếu mô tả", "Vui lòng nhập mô tả cho sản phẩm.");
      return;
    }
    if (name.isEmpty()
        || price.isEmpty()
        || anyNull(
            startDatePicker.getValue(),
            startHourCombo.getValue(),
            startMinuteCombo.getValue(),
            startSecondCombo.getValue(),
            endDatePicker.getValue(),
            endHourCombo.getValue(),
            endMinuteCombo.getValue(),
            endSecondCombo.getValue())) {
      lblStatus.setText("fill all fields");
      return;
    }

    String startNorm = readNormalizedStart();
    String endNorm = readNormalizedEnd();
    if (startNorm == null || endNorm == null) {
      lblStatus.setText("invalid start/end time");
      return;
    }
    LocalDateTime startDt = AddLotDateTimeHelper.parseNormalized(startNorm);
    LocalDateTime endDt = AddLotDateTimeHelper.parseNormalized(endNorm);
    if (startDt == null || endDt == null || !endDt.isAfter(startDt)) {
      showAlert(Alert.AlertType.WARNING, "Invalid time range", "End time must be after start time.");
      return;
    }

    boolean dutch = isDutchSelected();
    String dutchDecrementForSubmit = "";
    if (dutch) {
      Double ceiling;
      Double reserve;
      Integer interval;
      try {
        ceiling = Double.parseDouble(price);
        reserve = Double.parseDouble(txtDutchReservePrice.getText().trim());
        interval = Integer.parseInt(txtDutchIntervalMinutes.getText().trim());
      } catch (NumberFormatException | NullPointerException ex) {
        lblStatus.setText("Thông số Dutch không hợp lệ");
        return;
      }
      AddLotDutchScheduleHelper.ValidationResult validation =
          AddLotDutchScheduleHelper.validateForSubmit(startDt, endDt, ceiling, reserve, interval);
      if (!validation.ok()) {
        showAlert(
            Alert.AlertType.WARNING,
            "Lịch Dutch không hợp lệ",
            AddLotErrorMessages.format(validation.errorCode()));
        return;
      }
      applyDerivedTickField(validation.derivedTick());
      dutchDecrementForSubmit = String.format("%.2f", validation.derivedTick());
    }

    AddLotSubmissionCoordinator.FormPayload payload =
        new AddLotSubmissionCoordinator.FormPayload(
            name,
            price,
            maxPriceText.isEmpty() ? "0" : maxPriceText,
            desc,
            category,
            startNorm,
            endNorm,
            dutch,
            txtDutchReservePrice != null ? txtDutchReservePrice.getText().trim() : "",
            dutchDecrementForSubmit,
            txtDutchIntervalMinutes != null ? txtDutchIntervalMinutes.getText().trim() : "",
            lotimageurl,
            editingItemId);

    submissionCoordinator.submitAsync(
        payload,
        new AddLotSubmissionCoordinator.ResultHandler() {
          @Override
          public void onEditSuccess() {
            showAlert(Alert.AlertType.INFORMATION, "Đã lưu", "Đã cập nhật sản phẩm chờ duyệt.");
            KhungController.returnFromLotEditor(true);
            clearForm();
          }

          @Override
          public void onCreateSuccess() {
            showAlert(
                Alert.AlertType.INFORMATION,
                "Item Submitted",
                "Your item has been submitted and is pending admin approval.");
            KhungController.returnFromAddLot(true);
            clearForm();
          }

          @Override
          public void onFailure(String message) {
            lblStatus.setText(message);
          }
        });
  }

  @FXML
  public void handleCancel(ActionEvent e) {
    if (editingItemId != null) {
      KhungController.returnFromLotEditor(false);
    } else {
      KhungController.returnFromAddLot(false);
    }
    clearForm();
  }

  public void clearForm() {
    resetFormFieldsKeepGen();
    editingItemId = null;
    applyAddModeTitles();
  }

  private void resetFormFieldsKeepGen() {
    uploadUiGen.incrementAndGet();
    txtName.clear();
    txtPrice.clear();
    txtMaxPrice.clear();
    if (txtDutchReservePrice != null) {
      txtDutchReservePrice.clear();
    }
    if (txtDutchDecrement != null) {
      txtDutchDecrement.clear();
    }
    if (txtDutchIntervalMinutes != null) {
      txtDutchIntervalMinutes.clear();
    }
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
    if (kindEnglishToggle != null) {
      kindEnglishToggle.setSelected(true);
    }
    refreshAuctionKindPanels();
    java.net.URL hutao = getClass().getResource("/images/Hutao.png");
    productImageView.setImage(hutao != null ? new Image(hutao.toExternalForm(), false) : null);
  }

  private void populateFromExistingItem(Item item) {
    AddLotItemFormMapper.applyItem(
        item,
        txtName,
        txtQuantity,
        txtPrice,
        txtMaxPrice,
        kindEnglishToggle,
        kindDutchToggle,
        txtDutchReservePrice,
        txtDutchIntervalMinutes,
        startDatePicker,
        startHourCombo,
        startMinuteCombo,
        startSecondCombo,
        endDatePicker,
        endHourCombo,
        endMinuteCombo,
        endSecondCombo,
        classifyComboBox,
        new AddLotItemFormMapper.ImageTarget(url -> lotimageurl = url, productImageView));
    refreshAuctionKindPanels();
  }

  private void wireDutchPreviewListeners() {
    Runnable refresh = this::refreshDutchSchedulePreview;
    if (txtPrice != null) {
      txtPrice.textProperty().addListener((obs, oldV, newV) -> refresh.run());
    }
    if (txtDutchReservePrice != null) {
      txtDutchReservePrice.textProperty().addListener((obs, oldV, newV) -> refresh.run());
    }
    if (txtDutchIntervalMinutes != null) {
      txtDutchIntervalMinutes.textProperty().addListener((obs, oldV, newV) -> refresh.run());
    }
    if (startDatePicker != null) {
      startDatePicker.valueProperty().addListener((obs, oldV, newV) -> refresh.run());
    }
    if (endDatePicker != null) {
      endDatePicker.valueProperty().addListener((obs, oldV, newV) -> refresh.run());
    }
    for (ComboBox<Integer> combo :
        new ComboBox[] {
          startHourCombo, startMinuteCombo, startSecondCombo,
          endHourCombo, endMinuteCombo, endSecondCombo
        }) {
      if (combo != null) {
        combo.valueProperty().addListener((obs, oldV, newV) -> refresh.run());
      }
    }
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
    refreshDutchSchedulePreview();
  }

  private void refreshDutchSchedulePreview() {
    if (lblDutchSchedulePreview == null) {
      return;
    }
    if (isEnglishSelected()) {
      lblDutchSchedulePreview.setText("");
      if (txtDutchDecrement != null) {
        txtDutchDecrement.clear();
      }
      return;
    }
    LocalDateTime start = readFormStartDateTime();
    LocalDateTime end = readFormEndDateTime();
    Double ceiling = AddLotFormParseHelper.optionalDouble(txtPrice != null ? txtPrice.getText() : null);
    Double reserve =
        AddLotFormParseHelper.optionalDouble(
            txtDutchReservePrice != null ? txtDutchReservePrice.getText() : null);
    Integer interval =
        AddLotFormParseHelper.optionalInt(
            txtDutchIntervalMinutes != null ? txtDutchIntervalMinutes.getText() : null);

    AddLotDutchScheduleHelper.Preview preview;
    if (ceiling == null || reserve == null || interval == null) {
      preview =
          AddLotDutchScheduleHelper.Preview.hint(
              "Chọn start, end, interval và reserve — bước giảm sẽ được tính tự động.");
    } else {
      preview = AddLotDutchScheduleHelper.buildPreview(start, end, ceiling, reserve, interval);
    }

    lblDutchSchedulePreview.setText(preview.text());
    lblDutchSchedulePreview.setStyle(preview.cssStyle());
    if (preview.derivedTick() != null) {
      applyDerivedTickField(preview.derivedTick());
    } else if (txtDutchDecrement != null) {
      txtDutchDecrement.clear();
    }
  }

  private void applyDerivedTickField(double tick) {
    if (txtDutchDecrement != null) {
      txtDutchDecrement.setText(String.format("%.2f", tick));
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

  private boolean isDutchSelected() {
    return kindDutchToggle != null && kindDutchToggle.isSelected();
  }

  private boolean isEnglishSelected() {
    return kindEnglishToggle == null || kindEnglishToggle.isSelected();
  }

  private String readNormalizedStart() {
    return AddLotDateTimeHelper.buildNormalized(
        startDatePicker.getValue(),
        startHourCombo.getValue(),
        startMinuteCombo.getValue(),
        startSecondCombo.getValue());
  }

  private String readNormalizedEnd() {
    return AddLotDateTimeHelper.buildNormalized(
        endDatePicker.getValue(),
        endHourCombo.getValue(),
        endMinuteCombo.getValue(),
        endSecondCombo.getValue());
  }

  private LocalDateTime readFormStartDateTime() {
    return AddLotDateTimeHelper.parseNormalized(readNormalizedStart());
  }

  private LocalDateTime readFormEndDateTime() {
    return AddLotDateTimeHelper.parseNormalized(readNormalizedEnd());
  }

  private static boolean anyNull(Object... values) {
    for (Object v : values) {
      if (v == null) {
        return true;
      }
    }
    return false;
  }

  private static void showAlert(Alert.AlertType type, String title, String msg) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(msg);
    alert.showAndWait();
  }
}
