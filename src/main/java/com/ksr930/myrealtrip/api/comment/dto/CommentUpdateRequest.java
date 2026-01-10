package com.ksr930.myrealtrip.api.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommentUpdateRequest {
    @NotNull
    private Long userId;

    @NotBlank
    @Size(max = 2000)
    private String content;
}
