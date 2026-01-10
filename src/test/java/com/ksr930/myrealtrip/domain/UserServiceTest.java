package com.ksr930.myrealtrip.domain;

import com.ksr930.myrealtrip.api.user.dto.UserResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.user.UserService;
import com.ksr930.myrealtrip.domain.user.UserRepository;
import com.ksr930.myrealtrip.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 생성/조회/수정이 정상 동작한다")
    void create_get_update_user() {
        UserResponse created = userService.createUser("Alice");
        UserResponse fetched = userService.getUserResponse(created.id());
        assertThat(fetched.name()).isEqualTo("Alice");

        UserResponse updated = userService.updateUser(created.id(), "Alice2");
        assertThat(updated.name()).isEqualTo("Alice2");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 시 NOT_FOUND 예외가 발생한다")
    void get_missing_user_throws() {
        assertThatThrownBy(() -> userService.getUserResponse(999L))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 삭제 시 엔티티가 제거된다")
    void delete_user_removes_entity() {
        User user = userRepository.save(new User("A"));

        userService.deleteUser(user.getId());
        assertThat(userRepository.findById(user.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 사용자 삭제 시 NOT_FOUND 예외가 발생한다")
    void delete_missing_user_throws() {
        assertThatThrownBy(() -> userService.deleteUser(999L))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
