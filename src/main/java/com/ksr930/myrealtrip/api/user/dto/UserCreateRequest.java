package com.ksr930.myrealtrip.api.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserCreateRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
}
