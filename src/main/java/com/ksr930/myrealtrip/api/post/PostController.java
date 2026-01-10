package com.ksr930.myrealtrip.api.post;

import com.ksr930.myrealtrip.api.post.dto.PostCreateRequest;
import com.ksr930.myrealtrip.api.post.dto.PostResponse;
import com.ksr930.myrealtrip.api.post.dto.PostUpdateRequest;
import com.ksr930.myrealtrip.common.dto.ApiResponse;
import com.ksr930.myrealtrip.domain.post.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<PostResponse>> createPost(@Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.status(201)
                .body(ApiResponse.success(201, postService.createPost(request.userId(), request.content())));
    }

    @PutMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(postService.updatePost(postId, request.content())));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostResponse>> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPost(postId)));
    }
}
