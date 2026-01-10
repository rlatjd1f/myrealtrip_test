package com.ksr930.myrealtrip.domain.follow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    Optional<Follow> findByFollowerIdAndFolloweeId(Long followerId, Long followeeId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Follow f where f.follower.id = :userId or f.followee.id = :userId")
    int deleteByUserId(@Param("userId") Long userId);
}
