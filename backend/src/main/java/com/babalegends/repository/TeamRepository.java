package com.babalegends.repository;

import com.babalegends.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {

    @Modifying(flushAutomatically = true)
    @Query("delete from Team t where t.match.id = :matchId")
    void deleteByMatchId(UUID matchId);
}
