package com.auction.server.utils;

import com.auction.server.context.HandlerContext;
import com.auction.shared.link.ErrorResponse;
import com.auction.shared.link.Request;
import com.auction.shared.link.RequestType;
import com.auction.shared.link.Response;
import com.auction.shared.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bộ điều phối (Dispatcher) chịu trách nhiệm:
 * <ul>
 *   <li>Đăng ký các {@link RequestHandler} tương ứng với từng loại request (action).</li>
 *   <li>Tiếp nhận {@link Request}, validate và chuyển tiếp đến handler phù hợp.</li>
 *   <li>Xử lý lỗi và trả về {@link Response} tương ứng.</li>
 * </ul>
 *
 * <p>Class này thread-safe nhờ sử dụng {@link ConcurrentHashMap} để lưu trữ handler.</p>
 */
public class RequestDispatcher {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);

  /**
   * Map lưu trữ các handler theo command (action).
   * Key được chuẩn hóa về uppercase.
   */
  private final Map<RequestType, RequestHandler> handlers = new ConcurrentHashMap<>();

  /**
   * Đăng ký một {@link RequestHandler} cho một loại request cụ thể.
   *
   * @param type yêu cầu
   * @param handler handler xử lý request tương ứng
   */
  public void register(RequestType type, RequestHandler handler) {
    if (type == null || handler == null) {
      LOGGER.error("Không thể đăng ký Handler: Command hoặc Handler bị null.");
      return;
    }
    handlers.put(type, handler);
    LOGGER.info("Đã đăng ký Handler cho lệnh: [{}]", type);
  }

  /**
   * Điều phối request đến handler tương ứng sau khi validate.
   *
   * <p>Quy trình:
   * <ol>
   *   <li>Validate request bằng các rule toàn cục.</li>
   *   <li>Tìm handler theo action.</li>
   *   <li>Thực thi handler và trả về response.</li>
   *   <li>Nếu có lỗi xảy ra, trả về {@link ErrorResponse}.</li>
   * </ol>
   *
   * @param request request cần xử lý
   * @return {@link Response} kết quả xử lý hoặc {@link ErrorResponse} nếu có lỗi
   */
  public Response dispatch(Request request, HandlerContext handlerContext) {
    boolean requestError = RequestValidator.validate(request, RequestRules);
    if (!requestError) {
      LOGGER.warn("Request không vượt qua bước validate");
      return new ErrorResponse("request không hợp lệ");
    }

    boolean clientError = RequestValidator.validate(request, RequestRules);
    if (!clientError) {
      LOGGER.warn("Client không vượt qua bước validate");
      return new ErrorResponse("client không đủ tư cách");
    }

    RequestType command = request.getAction();
    RequestHandler handler = handlers.get(command);

    if (handler == null) {
      LOGGER.warn("Không tìm thấy handler cho lệnh [{}]", command);
      return new ErrorResponse("Lệnh không được hỗ trợ.");
    }

    try {
      return handler.handle(request, handlerContext);
    } catch (Exception e) {
      LOGGER.error("Lỗi thực thi lệnh [{}]: {}", command, e.getMessage(), e);
      return new ErrorResponse("Lỗi nội bộ hệ thống.");
    }
  }

  /**
   * Danh sách các rule validate áp dụng cho mọi request.
   */
  private final List<RequestValidator.ValidationRule> RequestRules = List.of(
    new RequestValidator.ValidationRule(req -> req == null, "Request rỗng"),
    new RequestValidator.ValidationRule(req -> req.getAction() == null, "Thiếu tên lệnh")
  );

  private final List<ClientValidator.ValidationRule> ClientRulse = List.of(
    new ClientValidator.ValidationRule(user -> user.getStatus().equals(UserStatus.LOCKED), "User bị khoá")
  );
}
