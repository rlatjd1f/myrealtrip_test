package com.ksr930.myrealtrip.domain.user;

import com.ksr930.myrealtrip.api.user.dto.UserResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOT_FOUND));
    }

    public UserResponse getUserResponse(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse createUser(String name) {
        User user = userRepository.save(new User(name));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, String name) {
        User user = getUser(userId);
        user.updateName(name);
        return UserResponse.from(user);
    }
}
