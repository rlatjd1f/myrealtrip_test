package com.ksr930.myrealtrip.api.user.dto;

import jakarta.validation.constraints.NotNull;

public record FollowRequest(
        @NotNull Long followerId,
        @NotNull Long followeeId
) {
}
