package com.ksr930.myrealtrip.domain;

import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.follow.FollowRepository;
import com.ksr930.myrealtrip.domain.follow.FollowService;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class FollowServiceTest {
    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("팔로우는 관계를 생성하고 중복 팔로우를 차단한다")
    void follow_creates_relation_and_blocks_duplicates() {
        User follower = userRepository.save(new User("A"));
        User followee = userRepository.save(new User("B"));

        followService.follow(follower.getId(), followee.getId());
        assertThat(followRepository.existsByFollowerIdAndFolloweeId(follower.getId(), followee.getId())).isTrue();

        assertThatThrownBy(() -> followService.follow(follower.getId(), followee.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CONFLICT);
    }

    @Test
    @DisplayName("자기 자신 팔로우 요청은 거부된다")
    void follow_blocks_self_follow() {
        User user = userRepository.save(new User("A"));

        assertThatThrownBy(() -> followService.follow(user.getId(), user.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("언팔로우는 관계가 없으면 NOT_FOUND 예외가 발생한다")
    void unfollow_requires_existing_relation() {
        User follower = userRepository.save(new User("A"));
        User followee = userRepository.save(new User("B"));

        assertThatThrownBy(() -> followService.unfollow(follower.getId(), followee.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
