package com.ksr930.myrealtrip.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ErrorDetail {
    private final List<FieldViolation> fieldErrors;
}
