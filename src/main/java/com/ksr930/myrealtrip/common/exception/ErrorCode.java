package com.ksr930.myrealtrip.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    INVALID_REQUEST("INVALID_REQUEST", "요청이 올바르지 않습니다."),
    NOT_FOUND("NOT_FOUND", "대상을 찾을 수 없습니다."),
    CONFLICT("CONFLICT", "요청이 충돌했습니다.");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
