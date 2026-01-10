package com.ksr930.myrealtrip.common.dto;

import com.ksr930.myrealtrip.common.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ApiResponse<T> {
    private final int status;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(int status, String code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "OK", "성공", data);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, T data) {
        return new ApiResponse<>(errorCode.getHttpStatus().value(), errorCode.getCode(), errorCode.getMessage(), data);
    }
}
