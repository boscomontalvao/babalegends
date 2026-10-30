package com.babalegends.repository;

import com.babalegends.entity.Match;
import com.babalegends.entity.enums.MatchStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, UUID> {

    @Query("select m from Match m join fetch m.group  where m.id = :matchId")
    Optional<Match> findByIdWithGroup(UUID matchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Match> findWithLockById(UUID matchId);

    List<Match> findByGroupIdOrderByScheduledAtDesc(UUID groupId);

    List<Match> findByGroupIdAndStatus(UUID groupId, MatchStatus status);

    @Query("""
            select m.id from Match m
            where m.status = :status and m.votesAppliedAt is null and m.completedAt <= :completedBefore
            """)
    List<UUID> findMatchIdsToCloseVoting(MatchStatus status, Instant completedBefore);
}
