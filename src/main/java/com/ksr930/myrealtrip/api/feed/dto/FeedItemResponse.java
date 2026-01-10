package com.ksr930.myrealtrip.api.feed.dto;

import com.ksr930.myrealtrip.domain.post.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FeedItemResponse {
    private final Long postId;
    private final Long userId;
    private final String content;
    private final LocalDateTime createdAt;

    private FeedItemResponse(Long postId, Long userId, String content, LocalDateTime createdAt) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static FeedItemResponse from(Post post) {
        return new FeedItemResponse(
                post.getId(),
                post.getUser().getId(),
                post.getContent(),
                post.getCreatedAt()
        );
    }
}
