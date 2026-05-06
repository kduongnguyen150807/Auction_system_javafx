package com.auction.server.utils;

import com.auction.shared.link.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Predicate;

/**
 * Lớp hỗ trợ kiểm tra tính hợp lệ của Request dựa trên danh sách các điều kiện.
 */
public class RequestValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestValidator.class);
    /**
     * Kiểm tra Request dựa trên một danh sách các quy tắc.
     * 
     * @param request Đối tượng cần kiểm tra.
     * @param validators Danh sách các quy tắc (Rule). Mỗi quy tắc gồm một điều kiện và một lỗi trả về.
     * @return boolean trả về giá trị boolean
     */
    public static boolean validate(Request request, List<ValidationRule> validators) {
        for (ValidationRule rule : validators) {
            if (rule.condition().test(request)) {
                LOGGER.info("Validation rule {} validated", rule.errorMessage());
                return false;
            }
        }
        return true;
    }

    /**
     * Record đại diện cho một quy tắc kiểm tra.
     * condition: Trả về true nếu Request BỊ LỖI.
     */
    public record ValidationRule(Predicate<Request> condition, String errorMessage) {}
}