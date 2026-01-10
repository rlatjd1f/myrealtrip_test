package com.ksr930.myrealtrip.api;

import com.ksr930.myrealtrip.api.user.UserController;
import com.ksr930.myrealtrip.api.user.dto.UserResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.common.exception.GlobalExceptionHandler;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.ksr930.myrealtrip.api.user.dto.UserCreateRequest;
import com.ksr930.myrealtrip.api.user.dto.UserUpdateRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("사용자 생성 API는 성공 응답을 반환한다")
    void create_user_returns_ok_response() throws Exception {
        when(userService.createUser(anyString())).thenReturn(UserResponse.from(new User("Alice")));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest("Alice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.name").value("Alice"));
    }

    @Test
    @DisplayName("사용자 생성 API는 name 누락/공백이면 에러를 반환한다")
    void create_user_validation_error() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserCreateRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("사용자 조회 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void get_user_not_found() throws Exception {
        when(userService.getUserResponse(eq(999L))).thenThrow(new ApiException(ErrorCode.NOT_FOUND));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 삭제 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void delete_user_not_found() throws Exception {
        doThrow(new ApiException(ErrorCode.NOT_FOUND)).when(userService).deleteUser(eq(999L));

        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("사용자 삭제 API는 성공 시 204 응답을 반환한다")
    void delete_user_success() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("사용자 수정 API는 name 누락/공백이면 에러를 반환한다")
    void update_user_validation_error() throws Exception {
        mockMvc.perform(patch("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("사용자 수정 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void update_user_not_found() throws Exception {
        when(userService.updateUser(eq(999L), anyString()))
                .thenThrow(new ApiException(ErrorCode.NOT_FOUND));

        mockMvc.perform(patch("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest("Alice"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
