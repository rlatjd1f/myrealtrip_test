package com.ksr930.myrealtrip.domain.comment;

import com.ksr930.myrealtrip.api.comment.dto.CommentResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.post.PostRepository;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    @Transactional
    public CommentResponse createComment(Long postId, Long userId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        User user = userService.getUser(userId);
        Comment comment = commentRepository.save(new Comment(post, user, content));
        return CommentResponse.from(comment);
    }

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getComments(Long postId, int page, int size) {
        List<CommentResponse> items = commentRepository.findByPostIdOrderByCreatedAtDesc(
                        postId,
                        PageRequest.of(page, size))
                .map(CommentResponse::from)
                .getContent();
        Long nextCursor = items.isEmpty() ? null : items.get(items.size() - 1).getId();
        return new PageResponse<>(items, nextCursor, items.size());
    }

    @Transactional
    public CommentResponse updateComment(Long commentId, Long userId, String content) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        comment.updateContent(content);
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        commentRepository.delete(comment);
    }
}
