package com.datn.ticket.exception;

import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceException;
import lombok.Getter;
import org.springframework.dao.EmptyResultDataAccessException;

@Getter
public class AppException extends RuntimeException {
    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    private ErrorCode errorCode;

    public AppException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public static AppException from(EmptyResultDataAccessException e, ErrorCode errorCode) {
        return new AppException(errorCode, e);
    }
    public void setErrorCode(ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
