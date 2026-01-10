package com.ksr930.myrealtrip.api.user.dto;

import com.ksr930.myrealtrip.domain.user.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String name,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getCreatedAt());
    }
}
