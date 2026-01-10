package com.ksr930.myrealtrip.common.exception;

import java.util.List;

public record ErrorDetail(
        List<FieldViolation> fieldErrors
) {
}
