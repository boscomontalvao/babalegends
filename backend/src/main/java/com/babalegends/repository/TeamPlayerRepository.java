package com.babalegends.repository;

import com.babalegends.entity.TeamPlayer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, UUID> {

    @Query("""
            select tp from TeamPlayer tp
            join fetch tp.user join fetch tp.team
            where tp.team.match.id = :matchId
            order by tp.team.name, tp.user.name
            """)
    List<TeamPlayer> findAllByMatchIdWithUserAndTeam(UUID matchId);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from TeamPlayer tp
            where tp.team.id in (select t.id from Team t where t.match.id = :matchId)
            """)
    void deleteByMatchId(UUID matchId);
}
