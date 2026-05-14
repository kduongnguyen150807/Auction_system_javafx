package com.auction.server.utils;

import com.auction.server.context.HandlerContext;
import com.auction.shared.linkv2.Request;
import com.auction.shared.linkv2.RequestType;
import com.auction.shared.linkv2.Response;
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
  private final Map<RequestType, RequestHandler<?, ?>> handlers = new ConcurrentHashMap<>();

  /**
   * Đăng ký một {@link RequestHandler} cho một loại request cụ thể.
   *
   * @param type yêu cầu
   * @param handler handler xử lý request tương ứng
   */
  public void register(RequestType type, RequestHandler<?, ?> handler) {
    LOGGER.info(
      "Registered handlers: {}",
      handlers.keySet()
    );
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
   * </ol>
   *
   * @param request request cần xử lý
   * @return {@link Response} kết quả xử lý hoặc
   */
  public <REQ, RES> Response<RES> dispatch(Request<REQ> request, HandlerContext handlerContext) {
    LOGGER.info(
      "Request type: {} ({})",
      request.getType(),
      request.getType().getClass()
    );
    // ===== Validate request =====
    boolean requestError = RequestValidator.validate(request, requestRules);
    if (!requestError) {
      LOGGER.warn("Request không vượt qua bước validate");
      return Response.error(request.getId(), "INVALID REQUEST");
    }
    // ===== Validate current user =====
    if (handlerContext.getUser()!=null) {
      boolean clientError = ClientValidator.validate(handlerContext.getUser(), ClientRule);
      if (!clientError) {
        LOGGER.warn("Client không vượt qua bước validate");
        return Response.error(request.getId(), "LOCKED CLIENT");
      }
    }
    // ===== Find handler =====
    RequestType type = request.getType();
    RequestHandler<REQ, RES> handler = (RequestHandler<REQ, RES>) handlers.get(type);

    if (handler == null) {
      LOGGER.warn("Không tìm thấy handler cho lệnh [{}]", type);
      return Response.error(request.getId(),"Lệnh không được hỗ trợ.");
    }
    // ===== Validate context =====

    if (handler instanceof Authorizable) {
      boolean success = ((Authorizable) handler).authorize(handlerContext, request);
      if (!success) {
        return Response.error(request.getId(), "Bạn không đủ thẩm quyền");
      }
    }

    // ===== Execute handler =====
    try {
      return handler.handle(request, handlerContext);
    } catch (Exception e) {
      LOGGER.error("Lỗi thực thi lệnh [{}]: {}", type, e.getMessage(), e);
      return Response.error(request.getId(), "Lỗi nội bộ hệ thống.");
    }
  }

  /**
   * Danh sách các rule validate áp dụng cho mọi request.
   */
  private final List<RequestValidator.ValidationRule>
    requestRules = List.of(

    new RequestValidator.ValidationRule(
      req -> req == null,
      "Request is null"
    ),

    new RequestValidator.ValidationRule(
      req -> req.getType() == null,
      "Request action is null"
    )
  );

  private final List<ClientValidator.ValidationRule> ClientRule = List.of(
    new ClientValidator.ValidationRule(
      user -> user.getStatus().equals(UserStatus.LOCKED),
      "User bị khoá")
  );
}
