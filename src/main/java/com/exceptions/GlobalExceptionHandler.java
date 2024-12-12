package com.exceptions;

import cn.dev33.satoken.exception.NotLoginException;
import cn.hutool.core.util.ObjectUtil;
import com.enums.ErrorEnum;
import com.rk.utils.R;
import com.utils.I18nMessageUtil;
import com.utils.RpcResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.annotation.Order;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
@Order(1)
public class GlobalExceptionHandler {

    private final I18nMessageUtil i18nMessageUtil;

    public GlobalExceptionHandler(I18nMessageUtil i18nMessageUtil) {
        this.i18nMessageUtil = i18nMessageUtil;
    }

    /**
     * 请求方式不支持
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public RpcResult<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e,
                                                               HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',不支持'{}'请求", requestURI, e.getMethod());
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), "不支持的请求");
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BizException.class)
    public RpcResult<Void> handleServiceException(BizException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return ObjectUtil.isNotNull(code) ? RpcResult.error(code, e.getMessage()) : RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(ApiException.class)
    public RpcResult<Void> handleServiceException(ApiException e, HttpServletRequest request) {
        log.error(e.getMessage(), e);
        Integer code = e.getCode();
        return ObjectUtil.isNotNull(code) ? RpcResult.error(code, e.getMessage()) : RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 请求路径中缺少必需的路径变量
     */
    @ExceptionHandler(MissingPathVariableException.class)
    public RpcResult<Void> handleMissingPathVariableException(MissingPathVariableException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求路径中缺少必需的路径变量'{}',发生系统异常.", requestURI, e);
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), String.format("请求路径中缺少必需的路径变量[%s]", e.getVariableName()));
    }

    /**
     * 请求参数类型不匹配
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public RpcResult<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求参数类型不匹配'{}',发生系统异常.", requestURI, e);
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), String.format("请求参数类型不匹配，参数[%s]要求类型为：'%s'，但输入值为：'%s'", e.getName(), e.getRequiredType().getName(), e.getValue()));
    }

    /**
     * 拦截未知的运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public RpcResult<Void> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生未知异常.", requestURI, e);
        String message = I18nMessageUtil.getMessage(ErrorEnum.SYSTEM_ERROR.getI18nKey());
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 系统异常
     */
    @ExceptionHandler(Exception.class)
    public RpcResult<Void> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',发生系统异常.", requestURI, e);
        String message = I18nMessageUtil.getMessage(ErrorEnum.SYSTEM_ERROR.getI18nKey());
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(BindException.class)
    public RpcResult<Void> handleBindException(BindException e) {
        log.error(e.getMessage(), e);
        String message = e.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).filter(Objects::nonNull).collect(Collectors.joining(", "));
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 自定义验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RpcResult<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error(e.getMessage(), e);
        // String message = e.getBindingResult().getFieldError().getDefaultMessage();
        BindingResult bindingResult = e.getBindingResult();
        // String errorMessage = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getField() + ":" + fieldError.getDefaultMessage() + " ").collect(Collectors.joining());
        String errorMessage = bindingResult.getFieldErrors().stream().map(fieldError -> fieldError.getDefaultMessage() + " ").collect(Collectors.joining());
        return RpcResult.error(ErrorEnum.SYSTEM_ERROR.getCode(), errorMessage);
    }

    /**
     * 认证失败
     */
    @ExceptionHandler(NotLoginException.class)
    public RpcResult<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.error("请求地址'{}',认证失败'{}',无法访问系统资源", requestURI, e.getMessage());
        String message = I18nMessageUtil.getMessage(ErrorEnum.AUTH_ERROR.getI18nKey());
        return RpcResult.error(ErrorEnum.AUTH_ERROR.getCode(), message);
    }

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public R handleServiceException(BusinessException e, HttpServletRequest request, HttpServletResponse response) {
        log.error(e.getLogMessage());
        log.error(e.getMessage(), e);
        String message = I18nMessageUtil.getMessage(e.getI18nKey());
        return R.error(e.getCode(), message);
    }
}
