package com.ksr930.myrealtrip.api.comment;

import com.ksr930.myrealtrip.api.comment.dto.CommentCreateRequest;
import com.ksr930.myrealtrip.api.comment.dto.CommentResponse;
import com.ksr930.myrealtrip.api.comment.dto.CommentUpdateRequest;
import com.ksr930.myrealtrip.common.dto.ApiResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.domain.comment.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping
    public ApiResponse<CommentResponse> createComment(@Valid @RequestBody CommentCreateRequest request) {
        return ApiResponse.success(
                commentService.createComment(request.getPostId(), request.getUserId(), request.getContent()));
    }

    @GetMapping
    public ApiResponse<PageResponse<CommentResponse>> getComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(commentService.getComments(postId, page, size));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(@PathVariable Long commentId, @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        return ApiResponse.success(commentService.updateComment(
                commentId,
                request.getUserId(),
                request.getContent()));
    }
}
