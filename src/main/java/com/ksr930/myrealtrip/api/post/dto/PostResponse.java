package com.ksr930.myrealtrip.api.post.dto;

import com.ksr930.myrealtrip.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        Long userId,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getUser().getId(),
                post.getContent(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
