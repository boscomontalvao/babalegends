package com.babalegends.repository;

import com.babalegends.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

    boolean existsByMatchIdAndVoterId(UUID matchId, UUID voterId);

    List<Vote> findByMatchId(UUID matchId);
}
