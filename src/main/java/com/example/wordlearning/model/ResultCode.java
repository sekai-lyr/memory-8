package com.example.wordlearning.model;

public enum ResultCode {
    SUCCESS(200, "Success"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    PREMIUM_REQUIRED(402, "Premium membership required"),
    NOT_FOUND(404, "Not found"),
    ERROR(500, "Server error");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
