package com.fancier.picture.backend.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.fancier.picture.backend.common.BaseResponse;
import com.fancier.picture.backend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.xml.ws.Response;

/**
 * 全局异常处理器
 * @author <a href="https://github.com/hola1009">fancier</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotLoginException.class)
    public BaseResponse<?> notLoginException(NotLoginException e) {
        log.error("NotLoginException", e);
        return ResultUtils.error(ErrorCode.NOT_LOGIN_ERROR, e.getMessage());
    }

    @ExceptionHandler(NotPermissionException.class)
    public BaseResponse<?> notPermissionExceptionHandler(NotPermissionException e) {
        log.error("NotPermissionException", e);
        return ResultUtils.error(ErrorCode.NO_AUTH_ERROR, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public BaseResponse<?> illegalArgumentExceptionHandler(IllegalArgumentException e) {
        log.error("IllegalArgumentException", e);
        return ResultUtils.error(ErrorCode.PARAM_ERROR, e.getMessage());
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class })
    public BaseResponse<?> methodArgumentNotValidExceptionException(MethodArgumentNotValidException e) {
        log.error("methodArgumentNotValidException", e);
        return ResultUtils.error(ErrorCode.PARAM_ERROR, e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }
}
