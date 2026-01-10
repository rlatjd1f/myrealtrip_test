package com.ksr930.myrealtrip.common.dto;

import java.util.List;

public record PageResponse<T>(
        List<T> items,
        Long nextCursor,
        int size
) {
}
