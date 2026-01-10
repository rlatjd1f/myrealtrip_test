package com.ksr930.myrealtrip.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("select p from Post p join Follow f on p.user.id = f.followee.id " +
            "where f.follower.id = :followerId " +
            "and (:cursorId is null or p.id < :cursorId) " +
            "order by p.id desc")
    Page<Post> findFeedByFollowerId(
            @Param("followerId") Long followerId,
            @Param("cursorId") Long cursorId,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Post p where p.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
