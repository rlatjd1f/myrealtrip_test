package com.ksr930.myrealtrip.api;

import com.ksr930.myrealtrip.api.comment.CommentController;
import com.ksr930.myrealtrip.api.comment.dto.CommentResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.common.exception.GlobalExceptionHandler;
import com.ksr930.myrealtrip.domain.comment.Comment;
import com.ksr930.myrealtrip.domain.comment.CommentService;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ksr930.myrealtrip.api.comment.dto.CommentCreateRequest;
import com.ksr930.myrealtrip.api.comment.dto.CommentUpdateRequest;
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
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@Import(GlobalExceptionHandler.class)
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    @DisplayName("댓글 생성 API는 성공 응답을 반환한다")
    void create_comment_returns_ok_response() throws Exception {
        User author = new User("A");
        Post post = new Post(author, "post");
        Comment comment = new Comment(post, author, "nice");
        when(commentService.createComment(anyLong(), anyLong(), anyString()))
                .thenReturn(CommentResponse.from(comment));

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest(1L, 1L, "nice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.content").value("nice"));
    }

    @Test
    @DisplayName("댓글 생성 API는 필수 필드 누락 시 에러를 반환한다")
    void create_comment_validation_error() throws Exception {
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest(1L, null, "nice"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("댓글 수정 API는 userId 누락 시 에러를 반환한다")
    void update_comment_validation_error() throws Exception {
        mockMvc.perform(patch("/api/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentUpdateRequest(null, "hi"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("댓글 삭제 API는 userId 누락 시 에러를 반환한다")
    void delete_comment_validation_error() throws Exception {
        mockMvc.perform(delete("/api/comments/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    @DisplayName("댓글 삭제 API는 성공 시 204 응답을 반환한다")
    void delete_comment_success() throws Exception {
        mockMvc.perform(delete("/api/comments/1")
                        .param("userId", "1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void update_comment_not_found() throws Exception {
        doThrow(new ApiException(ErrorCode.NOT_FOUND))
                .when(commentService).updateComment(eq(999L), anyLong(), anyString());

        mockMvc.perform(patch("/api/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentUpdateRequest(1L, "hi"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("댓글 삭제 API는 대상이 없으면 NOT_FOUND 에러를 반환한다")
    void delete_comment_not_found() throws Exception {
        doThrow(new ApiException(ErrorCode.NOT_FOUND))
                .when(commentService).deleteComment(eq(999L), anyLong());

        mockMvc.perform(delete("/api/comments/999")
                        .param("userId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

}
