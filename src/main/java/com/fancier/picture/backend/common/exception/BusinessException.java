package com.fancier.picture.backend.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * @author <a href="https://github.com/hola1009">fancier</a>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BusinessException(String message, int code) {
        super(message);
        this.code = code;
    }

}
