package com.enums;

public enum BaseResponseCode implements ResponseCode {

    FAILURE(-1, "失败"),
    SUCCESS(0, "成功");

    private int code;
    private String message;

    BaseResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
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
