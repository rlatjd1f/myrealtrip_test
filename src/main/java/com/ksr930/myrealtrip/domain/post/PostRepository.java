package com.ksr930.myrealtrip.domain.post;

import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p from Post p join Follow f on p.user.id = f.followee.id " +
            "where f.follower.id = :followerId " +
            "and (:cursorId is null " +
            "  or p.createdAt < :cursorCreatedAt " +
            "  or (p.createdAt = :cursorCreatedAt and p.id < :cursorId)) " +
            "order by p.createdAt desc, p.id desc")
    Slice<Post> findFeedByFollowerId(
            @Param("followerId") Long followerId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Query("select p.createdAt from Post p where p.id = :id")
    Optional<LocalDateTime> findCreatedAtById(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Post p where p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
