package com.ksr930.myrealtrip.api.feed.dto;

import com.ksr930.myrealtrip.domain.post.Post;

import java.time.LocalDateTime;

public record FeedItemResponse(
        Long postId,
        Long userId,
        String content,
        LocalDateTime createdAt
) {
    public static FeedItemResponse from(Post post) {
        return new FeedItemResponse(
                post.getId(),
                post.getUser().getId(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
