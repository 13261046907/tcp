package com.exceptions;


import com.enums.ErrorEnum;
import lombok.Data;

import java.text.MessageFormat;

@Data
public class BusinessException extends RuntimeException {
    private int code;
    private String i18nKey;
    private String message;
    private String logMessage;
    public BusinessException(ErrorEnum errorEnum, String logMessage) {
        this.code = errorEnum.getCode();
        this.i18nKey = errorEnum.getI18nKey();
        this.message = errorEnum.getMessage();
        this.logMessage = logMessage;
    }

    public Throwable fillInStackTrace() {
        return this;
    }

    public String toString() {
        return MessageFormat.format("{0},{1}", this.code, this.message);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getI18nKey() {
        return this.i18nKey;
    }

    public void setI18nKey(String i18nKey) {
        this.i18nKey = i18nKey;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLogMessage() {
        return this.logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }
}
