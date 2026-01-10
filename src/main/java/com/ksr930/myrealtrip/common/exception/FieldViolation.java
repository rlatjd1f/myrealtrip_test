package com.ksr930.myrealtrip.common.exception;

public record FieldViolation(
        String field,
        String message
) {
}
