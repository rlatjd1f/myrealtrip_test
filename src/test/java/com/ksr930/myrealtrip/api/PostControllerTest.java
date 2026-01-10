package com.ksr930.myrealtrip.api;

import com.ksr930.myrealtrip.api.post.PostController;
import com.ksr930.myrealtrip.api.post.dto.PostResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.common.exception.GlobalExceptionHandler;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.post.PostService;
import com.ksr930.myrealtrip.domain.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksr930.myrealtrip.api.post.dto.PostCreateRequest;
import com.ksr930.myrealtrip.api.post.dto.PostUpdateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
@Import(GlobalExceptionHandler.class)
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("포스트 생성 API는 성공 응답을 반환한다")
    void create_post_returns_ok_response() throws Exception {
        User author = new User("A");
        Post post = new Post(author, "hello");
        when(postService.createPost(anyLong(), anyString())).thenReturn(PostResponse.from(post));

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostCreateRequest(1L, "hello"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.content").value("hello"));
    }

    @Test
    @DisplayName("포스트 생성 API는 userId 누락 시 에러를 반환한다")
    void create_post_validation_error() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostCreateRequest(null, "hello"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("포스트 수정 API는 content 공백이면 에러를 반환한다")
    void update_post_validation_error() throws Exception {
        mockMvc.perform(put("/api/posts/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostUpdateRequest(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("포스트 조회 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void get_post_not_found() throws Exception {
        when(postService.getPost(eq(999L))).thenThrow(new ApiException(ErrorCode.NOT_FOUND));

        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("포스트 수정 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void update_post_not_found() throws Exception {
        when(postService.updatePost(eq(999L), anyString()))
                .thenThrow(new ApiException(ErrorCode.NOT_FOUND));

        mockMvc.perform(put("/api/posts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostUpdateRequest("update"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
