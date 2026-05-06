package com.auction.server.utils;

import com.auction.server.context.HandlerContext;
import com.auction.shared.link.Request;
import com.auction.shared.link.Response;

/**
 * Giao diện chung cho tất cả các bộ xử lý yêu cầu từ Client.
 */
@FunctionalInterface
public interface RequestHandler {
    /**
     * Xử lý yêu cầu và trả về phản hồi tương ứng.
     *
     * @param request Đối tượng yêu cầu chứa lệnh và dữ liệu.
     * @param handlerContext
     * @return Đối tượng phản hồi sau khi xử lý logic nghiệp vụ.
     * @throws Exception Cho phép ném ngoại lệ để RequestDispatcher xử lý tập trung.
     */
    Response handle(Request request, HandlerContext handlerContext) throws Exception;
}