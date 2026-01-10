package com.ksr930.myrealtrip.api.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PostUpdateRequest {
    @NotBlank
    @Size(max = 2000)
    private String content;
}
