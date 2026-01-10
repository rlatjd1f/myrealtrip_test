package com.ksr930.myrealtrip.domain.follow;

import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserService userService;

    @Transactional
    public void follow(Long followerId, Long followeeId) {
        if (followerId.equals(followeeId)) {
            throw new ApiException(ErrorCode.INVALID_REQUEST);
        }
        User follower = userService.getUser(followerId);
        User followee = userService.getUser(followeeId);
        boolean exists = followRepository.existsByFollowerIdAndFolloweeId(followerId, followeeId);
        if (exists) {
            throw new ApiException(ErrorCode.CONFLICT);
        }
        followRepository.save(new Follow(follower, followee));
    }

    @Transactional
    public void unfollow(Long followerId, Long followeeId) {
        Follow follow = followRepository.findByFollowerIdAndFolloweeId(followerId, followeeId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
        followRepository.delete(follow);
    }
}
