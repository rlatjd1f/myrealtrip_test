package com.ksr930.myrealtrip.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {
    private final List<T> items;
    private final Long nextCursor;
    private final int size;
}
