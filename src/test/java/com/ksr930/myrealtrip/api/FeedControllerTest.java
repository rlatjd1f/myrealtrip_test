package com.ksr930.myrealtrip.api;

import com.ksr930.myrealtrip.api.feed.FeedController;
import com.ksr930.myrealtrip.api.feed.dto.FeedItemResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.common.exception.GlobalExceptionHandler;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.post.PostService;
import com.ksr930.myrealtrip.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeedController.class)
@Import(GlobalExceptionHandler.class)
class FeedControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Test
    @DisplayName("피드 조회 API는 성공 응답을 반환한다")
    void get_feed_returns_ok_response() throws Exception {
        User author = new User("A");
        Post post = new Post(author, "hello");
        FeedItemResponse item = FeedItemResponse.from(post);
        PageResponse<FeedItemResponse> response = new PageResponse<>(List.of(item), null, 1);

        when(postService.getFeed(anyLong(), isNull(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/api/feed")
                        .param("followerId", "1")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("OK"))
                .andExpect(jsonPath("$.data.items[0].content").value("hello"));
    }

    @Test
    @DisplayName("피드 조회 API는 followerId 누락 시 에러를 반환한다")
    void get_feed_missing_follower_id() throws Exception {
        mockMvc.perform(get("/api/feed")
                        .param("size", "20"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
