package com.exceptions;

import com.enums.BaseResponseCode;

import java.text.MessageFormat;

public class BizException extends RuntimeException {

    private int code;
    private String message;

    public BizException(String message) {
        super();
        this.code = BaseResponseCode.FAILURE.getCode();
        this.message = message;
    }

    public BizException(int code, String message) {
        super();
        this.code = code;
        this.message = message;
    }

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
    @Override
    public String toString() {
        return MessageFormat.format("{0},{1}",this.code,this.message);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
