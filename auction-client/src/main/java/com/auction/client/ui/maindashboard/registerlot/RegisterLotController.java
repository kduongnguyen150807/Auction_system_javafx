package com.auction.client.ui.maindashboard.registerlot;

import com.auction.client.ui.utils.RequestHelper;
import com.auction.client.ui.utils.ValidationResult;
import com.auction.client.ui.utils.TimeUI;
import com.auction.shared.item.ItemType;
import com.auction.shared.link.Request;
import com.auction.shared.link.RequestType;
import com.auction.shared.link.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller quản lý giao diện đăng ký vật phẩm đấu giá.
 *
 * <p>Class này chịu trách nhiệm:
 * <ul>
 *   <li>Khởi tạo các thành phần giao diện</li>
 *   <li>Thu thập dữ liệu từ form</li>
 *   <li>Kiểm tra dữ liệu đầu vào</li>
 *   <li>Chuyển đổi dữ liệu form sang domain model</li>
 *   <li>Gửi yêu cầu đăng ký vật phẩm đến server</li>
 *   <li>Hiển thị trạng thái phản hồi lên giao diện</li>
 * </ul>
 *
 * <p>Controller chỉ đóng vai trò điều phối (orchestration),
 * các logic nghiệp vụ chính được tách sang:
 * <ul>
 *   <li>{@link LotFormValidator}</li>
 *   <li>{@link LotMapper}</li>
 *   <li>{@link RequestHelper}</li>
 * </ul>
 *
 * <p>Thiết kế này giúp:
 * <ul>
 *   <li>Tuân thủ nguyên lý Single Responsibility Principle</li>
 *   <li>Giảm coupling giữa UI và business logic</li>
 *   <li>Dễ mở rộng và bảo trì</li>
 * </ul>
 */
public class RegisterLotController {
  private static final Logger LOGGER = LoggerFactory.getLogger(RegisterLotController.class);
  /**
   * Mapper chuyển đổi dữ liệu từ {@link LotForm}
   * sang domain model.
   */
  private final LotMapper lotMapper = new LotMapper();

  /**
   * Danh sách category mặc định cho vật phẩm đấu giá.
   */
  private static final ObservableList<ItemType> DEFAULT_CATEGORIES =
    FXCollections.observableArrayList(
      ItemType.ART, ItemType.VEHICLE, ItemType.ELECTRONICS
    );

  @FXML private ImageView productImageView;
  @FXML private Label lblStatus;
  @FXML private TextField txtName, txtPrice, txtMaxPrice;
  @FXML private TextArea txtQuantity;
  @FXML private DatePicker startDatePicker, endDatePicker;
  @FXML private ComboBox<Integer> startHourCombo, startMinuteCombo, startSecondCombo;
  @FXML private ComboBox<Integer> endHourCombo, endMinuteCombo, endSecondCombo;
  @FXML private ComboBox<ItemType> classifyComboBox;

  private static RegisterLotController instance;
  private LotFormValidator lotFormValidator;

  /**
   * Khởi tạo controller sau khi FXML được load.
   *
   * <p>Method này:
   * <ul>
   *   <li>Khởi tạo validator</li>
   *   <li>Thiết lập dữ liệu cho các ComboBox</li>
   *   <li>Thiết lập validation cho các field số</li>
   * </ul>
   */
  @FXML
  private void initialize() {
    instance = this;
    lotFormValidator = new LotFormValidator();
    initTimeComboBoxes();
    initCategoryComboBox();
    setNumericFields();
  }

  /**
   * Khởi tạo dữ liệu cho các ComboBox thời gian.
   *
   * <p>Thiết lập:
   * <ul>
   *   <li>Danh sách giờ</li>
   *   <li>Danh sách phút</li>
   *   <li>Danh sách giây</li>
   *   <li>Giá trị mặc định</li>
   * </ul>
   */
  private void initTimeComboBoxes() {
    startHourCombo.setItems(TimeUI.HOURS);
    endHourCombo.setItems(TimeUI.HOURS);

    startMinuteCombo.setItems(TimeUI.MINS_SECS);
    startSecondCombo.setItems(TimeUI.MINS_SECS);
    endMinuteCombo.setItems(TimeUI.MINS_SECS);
    endSecondCombo.setItems(TimeUI.MINS_SECS);

    startHourCombo.getSelectionModel().select(0);
    startMinuteCombo.getSelectionModel().select(0);
    startSecondCombo.getSelectionModel().select(0);

    endHourCombo.getSelectionModel().select(0);
    endMinuteCombo.getSelectionModel().select(0);
    endSecondCombo.getSelectionModel().select(0);
  }

  /**
   * Khởi tạo ComboBox category.
   *
   * <p>Thiết lập:
   * <ul>
   *   <li>Danh sách category mặc định</li>
   *   <li>Converter hiển thị ItemType</li>
   * </ul>
   */
  private void initCategoryComboBox() {
    classifyComboBox.setItems(DEFAULT_CATEGORIES);
    classifyComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(ItemType object) {
        return object == null ? "" : object.name();
      }
      @Override
      public ItemType fromString(String string) {
        return ItemType.valueOf(string);
      }
    });
  }


  /**
   * Thiết lập validation cho các field số.
   *
   * <p>Chỉ cho phép nhập:
   * <ul>
   *   <li>Số nguyên</li>
   *   <li>Số thực</li>
   * </ul>
   *
   * <p>Nếu dữ liệu không hợp lệ,
   * field sẽ được rollback về giá trị cũ.
   */
  private void setNumericFields() {
    txtPrice.textProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal.matches("\\d*(\\.\\d*)?")) txtPrice.setText(oldVal);
    });
    txtMaxPrice.textProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal.matches("\\d*(\\.\\d*)?")) txtMaxPrice.setText(oldVal);
    });
  }

  /**
   * Thu thập dữ liệu từ giao diện
   * và đóng gói thành {@link LotForm}.
   *
   * @return dữ liệu form đã được thu thập
   */
  private LotForm collectData() {
    LotForm lotForm = new LotForm();
    lotForm.setName(txtName.getText().trim());
    lotForm.setDescription(txtQuantity.getText().trim());
    lotForm.setCategory(classifyComboBox.getValue());
    try {
      lotForm.setStartPrice(Double.parseDouble(txtPrice.getText()));
      if (!txtMaxPrice.getText().isEmpty()) {
        lotForm.setBuyNowPrice(Double.parseDouble(txtMaxPrice.getText()));
      }
    } catch (NumberFormatException e) {
      lotForm.setStartPrice(-1);
    }
    lotForm.setStartTime(TimeUI.combine(startDatePicker, startHourCombo, startMinuteCombo, startSecondCombo));
    lotForm.setEndTime(TimeUI.combine(endDatePicker, endHourCombo, endMinuteCombo, endSecondCombo));
    return lotForm;
  }

  @FXML
  private void handleChoosePicture() {

  }

  @FXML
  private void handleCancel() {

  }

  /**
   * Xử lý submit form đăng ký vật phẩm.
   *
   * <p>Flow xử lý:
   * <ol>
   *   <li>Thu thập dữ liệu từ UI</li>
   *   <li>Validate dữ liệu</li>
   *   <li>Map sang domain model</li>
   *   <li>Gửi request đến server</li>
   * </ol>
   */
  @FXML
  private void handleSubmit() {
    /* Collect data from ui */
    LotForm lotForm = collectData();

    /* Validate the data */
    ValidationResult result = lotFormValidator.validate(lotForm);

    if (!result.isValid()) {
      showStatus(result.message(), "red");
      return;
    }

    Request request = new Request(RequestType.REGISTER_LOT, lotMapper.map(lotForm));
    RequestHelper.sendRequest(request, this::onSendSuccess, this::onSendFailure);
  }

  /**
   * Callback được gọi khi gửi request thành công.
   *
   * @param response phản hồi từ server
   */
  private void onSendSuccess(Response response) {
    Platform.runLater(() -> {
      if (response.getStatus().equals(Response.OK)) {
        showStatus("Thành công: Vật phẩm đã được đăng ký!", "#28a745");
      } else {
        showStatus("Thất bại: " + response.getMessage(), "#dc3545"); // Red
      }
    });
  }

  /**
   * Callback được gọi khi gửi request thất bại.
   *
   * @param throwable exception xảy ra
   */
  private void onSendFailure(Throwable throwable) {
    Platform.runLater(() -> {
      LOGGER.error("Lỗi gửi yêu cầu đăng ký", throwable);
      showStatus("Lỗi kết nối: Không thể gửi yêu cầu đến Server.", "#dc3545");
    });
  }

  private void showStatus(String message, String colorHex) {
    lblStatus.setText(message);
    lblStatus.setStyle("-fx-text-fill: " + colorHex + ";");
  }
}
