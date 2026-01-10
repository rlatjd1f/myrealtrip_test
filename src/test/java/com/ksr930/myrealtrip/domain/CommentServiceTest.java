package com.ksr930.myrealtrip.domain;

import com.ksr930.myrealtrip.api.comment.dto.CommentResponse;
import com.ksr930.myrealtrip.common.dto.PageResponse;
import com.ksr930.myrealtrip.common.exception.ApiException;
import com.ksr930.myrealtrip.common.exception.ErrorCode;
import com.ksr930.myrealtrip.domain.comment.Comment;
import com.ksr930.myrealtrip.domain.comment.CommentRepository;
import com.ksr930.myrealtrip.domain.comment.CommentService;
import com.ksr930.myrealtrip.domain.post.Post;
import com.ksr930.myrealtrip.domain.post.PostRepository;
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
class CommentServiceTest {
    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("댓글 생성 후 목록에서 조회된다")
    void create_and_list_comments() {
        User author = userRepository.save(new User("A"));
        Post post = postRepository.save(new Post(author, "post"));

        CommentResponse created = commentService.createComment(post.getId(), author.getId(), "nice");
        PageResponse<CommentResponse> page = commentService.getComments(post.getId(), 0, 10);

        assertThat(page.items()).hasSize(1);
        assertThat(page.items().get(0).id()).isEqualTo(created.id());
    }

    @Test
    @DisplayName("댓글 수정/삭제는 작성자 검증을 통과해야 한다")
    void update_and_delete_require_author_match() {
        User author = userRepository.save(new User("A"));
        User other = userRepository.save(new User("B"));
        Post post = postRepository.save(new Post(author, "post"));
        Comment comment = commentRepository.save(new Comment(post, author, "nice"));

        assertThatThrownBy(() -> commentService.updateComment(comment.getId(), other.getId(), "change"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        CommentResponse updated = commentService.updateComment(comment.getId(), author.getId(), "change");
        assertThat(updated.content()).isEqualTo("change");

        assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), other.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        commentService.deleteComment(comment.getId(), author.getId());
        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 게시글/사용자에 댓글 생성 시 NOT_FOUND 예외가 발생한다")
    void create_comment_with_missing_post_or_user_throws() {
        User author = userRepository.save(new User("A"));
        Post post = postRepository.save(new Post(author, "post"));

        assertThatThrownBy(() -> commentService.createComment(999L, author.getId(), "no post"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);

        assertThatThrownBy(() -> commentService.createComment(post.getId(), 999L, "no user"))
                .isInstanceOf(ApiException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.NOT_FOUND);
    }
}
