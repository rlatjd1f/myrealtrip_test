package com.ksr930.myrealtrip.domain;

import com.ksr930.myrealtrip.api.feed.dto.FeedItemResponse;
import com.ksr930.myrealtrip.api.post.dto.PostResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.follow.FollowService;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.post.PostRepository;
import com.ksr930.myrealtrip.domain.post.PostService;
import com.ksr930.myrealtrip.domain.user.User;
import com.ksr930.myrealtrip.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class PostServiceTest {
    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowService followService;

    @Test
    @DisplayName("포스트 생성 후 조회할 수 있다")
    void create_and_get_post() {
        User author = userRepository.save(new User("A"));

        PostResponse created = postService.createPost(author.getId(), "hello");
        PostResponse fetched = postService.getPost(created.id());

        assertThat(fetched.content()).isEqualTo("hello");
        assertThat(fetched.userId()).isEqualTo(author.getId());
    }

    @Test
    @DisplayName("포스트 수정은 내용이 갱신된다")
    void update_post_updates_content() {
        User author = userRepository.save(new User("A"));
        Post post = postRepository.save(new Post(author, "before"));

        PostResponse updated = postService.updatePost(post.getId(), "after");
        assertThat(updated.content()).isEqualTo("after");
    }

    @Test
    @DisplayName("존재하지 않는 포스트 수정 시 NOT_FOUND 예외가 발생한다")
    void update_nonexistent_post_throws() {
        assertThatThrownBy(() -> postService.updatePost(999L, "no"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 포스트 생성 시 NOT_FOUND 예외가 발생한다")
    void create_post_with_missing_user_throws() {
        assertThatThrownBy(() -> postService.createPost(999L, "no user"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("피드는 id 내림차순과 커서 페이징을 따른다")
    void feed_returns_descending_id_order_with_cursor() {
        User follower = userRepository.save(new User("A"));
        User author1 = userRepository.save(new User("B"));
        User author2 = userRepository.save(new User("C"));

        followService.follow(follower.getId(), author1.getId());
        followService.follow(follower.getId(), author2.getId());

        Post post1 = postRepository.save(new Post(author1, "first"));
        Post post2 = postRepository.save(new Post(author2, "second"));
        Post post3 = postRepository.save(new Post(author1, "third"));

        PageResponse<FeedItemResponse> firstPage = postService.getFeed(follower.getId(), null, 2);
        List<Long> firstIds = firstPage.items().stream().map(FeedItemResponse::postId).toList();
        assertThat(firstIds).containsExactly(post3.getId(), post2.getId());

        PageResponse<FeedItemResponse> secondPage = postService.getFeed(follower.getId(), firstPage.nextCursor(), 2);
        List<Long> secondIds = secondPage.items().stream().map(FeedItemResponse::postId).toList();
        assertThat(secondIds).containsExactly(post1.getId());
    }
}
