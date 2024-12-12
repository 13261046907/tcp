package com.utils;

import com.enums.BaseResponseCode;
import com.enums.ResponseCode;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

public class RpcResult<T> implements Serializable {
    private static final long serialVersionUID = 2488663702267110932L;
    /**
     * 错误码
     */
    private int code;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 业务数据
     */
    private T data;

    private static final int SUCCESS_CODE = 0;

    public RpcResult() {
    }

    public static <T> RpcResult<T> error() {
        return error(BaseResponseCode.FAILURE);
    }

    public static <T> RpcResult<T> error(int code, String message) {
        return error(code, message, null);
    }

    public static <T> RpcResult<T> error(int code, String message, T data) {
        return new RpcResult<T>(code, message, data);
    }

    public static <T> RpcResult<T> error(BaseResponseCode errorCode) {
        return error(errorCode.getCode(), errorCode.getMessage());
    }

    public static <T> RpcResult<T> error(BaseResponseCode errorCode, T data) {
        return error(errorCode.getCode(), errorCode.getMessage(), data);
    }

    public static <T> RpcResult<T> error(BaseResponseCode errorCode, Object... msgValues) {
        String msg = errorCode.getMessage();
        if (msgValues != null && msgValues.length > 0) {
            msg = String.format(errorCode.getMessage(), msgValues);
        }
        return error(errorCode.getCode(), msg);
    }

    public static <T> RpcResult success() {
        return new RpcResult(SUCCESS_CODE, "");
    }

    public static <T> RpcResult<T> success(T data) {
        return new RpcResult<>(SUCCESS_CODE, "", data);
    }


    public RpcResult(T data) {
        this.code = BaseResponseCode.SUCCESS.getCode();
        this.message = BaseResponseCode.SUCCESS.getMessage();
        this.data = data;
    }

    public RpcResult(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public RpcResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * @param responseCode
     * @param <T>
     * @return
     */
    public static <T> RpcResult<T> failure(ResponseCode responseCode) {
        return new RpcResult<T>(responseCode.getCode(), responseCode.getMessage());
    }

    public boolean isSuccess() {
        return Objects.equals(BaseResponseCode.SUCCESS.getCode(), this.code);
    }

    public T checkResult(Function<RpcResult<T>, T> handler) {
        /**
         * 正常返回结果
         */
        if (this.code == BaseResponseCode.SUCCESS.getCode()) {
            return this.getData();
        }

        return Objects.isNull(handler) ? null : handler.apply(this);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "RpcResult{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
