package com.ksr930.myrealtrip.api.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FollowRequest {
    @NotNull
    private Long followerId;

    @NotNull
    private Long followeeId;
}
