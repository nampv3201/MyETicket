package com.datn.ticket.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Có lỗi xảy ra", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Lỗi không xác định", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Tài khoản đã tồn tại", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Tài khoản không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Yêu cầu xác thực tài khoản", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    SESSION_EXPIRED(1009, "Session timeout", HttpStatus.BAD_REQUEST),
    ITEM_NOT_FOUND(1010, "Không tìm thấy", HttpStatus.NOT_FOUND),
    EMAIL_EXISTED(1011, "Email đã được đăng ký", HttpStatus.BAD_REQUEST),
    ITEM_NOT_EXIST(1012, "Thông tin về chưa được đăng ký", HttpStatus.BAD_REQUEST);
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

}
