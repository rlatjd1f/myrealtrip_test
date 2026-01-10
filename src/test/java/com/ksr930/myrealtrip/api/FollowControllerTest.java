package com.ksr930.myrealtrip.api;

import com.ksr930.myrealtrip.api.user.FollowController;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.common.exception.GlobalExceptionHandler;
import com.ksr930.myrealtrip.domain.follow.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksr930.myrealtrip.api.user.dto.FollowRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FollowController.class)
@Import(GlobalExceptionHandler.class)
class FollowControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FollowService followService;

    @Test
    @DisplayName("팔로우 API는 성공 응답을 반환한다")
    void follow_returns_ok_response() throws Exception {
        mockMvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(1L, 2L))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("OK"));
    }

    @Test
    @DisplayName("팔로우 API는 followerId/followeeId 누락 시 에러를 반환한다")
    void follow_validation_error() throws Exception {
        mockMvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(null, null))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("언팔로우 API는 관계가 없으면 NOT_FOUND 에러를 반환한다")
    void unfollow_not_found_returns_error() throws Exception {
        doThrow(new ApiException(ErrorCode.NOT_FOUND))
                .when(followService).unfollow(anyLong(), anyLong());

        mockMvc.perform(delete("/api/follows")
                        .param("followerId", "1")
                        .param("followeeId", "2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("언팔로우 API는 성공 시 204 응답을 반환한다")
    void unfollow_success_returns_no_content() throws Exception {
        mockMvc.perform(delete("/api/follows")
                        .param("followerId", "1")
                        .param("followeeId", "2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("팔로우 API는 중복 관계일 때 CONFLICT 에러를 반환한다")
    void follow_conflict_returns_error() throws Exception {
        doThrow(new ApiException(ErrorCode.CONFLICT))
                .when(followService).follow(anyLong(), anyLong());

        mockMvc.perform(post("/api/follows")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new FollowRequest(1L, 2L))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }
}
