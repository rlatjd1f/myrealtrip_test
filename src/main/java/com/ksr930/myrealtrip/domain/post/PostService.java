package com.ksr930.myrealtrip.domain.post;

import com.ksr930.myrealtrip.api.feed.dto.FeedItemResponse;
import com.ksr930.myrealtrip.api.post.dto.PostResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;

    @Transactional
    public PostResponse createPost(Long userId, String content) {
        User user = userService.getUser(userId);
        Post post = postRepository.save(new Post(user, content));
        return PostResponse.from(post);
    }

    @Transactional
    public PostResponse updatePost(Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        post.updateContent(content);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedItemResponse> getFeed(Long followerId, Long cursorId, int size) {
        LocalDateTime cursorCreatedAt = null;
        if (cursorId != null) {
            cursorCreatedAt = postRepository.findCreatedAtById(cursorId)
                    .orElseThrow(() -> new ApiException(ErrorCode.INVALID_REQUEST));
        }

        Slice<Post> slice = postRepository.findFeedByFollowerId(
                followerId,
                cursorCreatedAt,
                cursorId,
                PageRequest.of(0, size));
        List<FeedItemResponse> items = slice.getContent().stream()
                .map(FeedItemResponse::from)
                .toList();
        Long nextCursor = slice.hasNext() ? items.getLast().postId() : null;
        return new PageResponse<>(items, nextCursor, items.size());
    }
}
