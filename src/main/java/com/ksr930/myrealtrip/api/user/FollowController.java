package com.ksr930.myrealtrip.api.user;

import com.ksr930.myrealtrip.api.user.dto.FollowRequest;
import com.ksr930.myrealtrip.common.dto.ApiResponse;
import com.ksr930.myrealtrip.domain.follow.FollowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> follow(@Valid @RequestBody FollowRequest request) {
        followService.follow(request.followerId(), request.followeeId());
        return ResponseEntity.status(201).body(ApiResponse.success(201, null));
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestParam Long followerId, @RequestParam Long followeeId) {
        followService.unfollow(followerId, followeeId);
        return ResponseEntity.noContent().build();
    }
}
