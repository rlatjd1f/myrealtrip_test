package com.ksr930.myrealtrip.api.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 2000) String content
) {
}
