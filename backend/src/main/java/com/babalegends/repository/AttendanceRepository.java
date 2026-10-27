package com.babalegends.repository;

import com.babalegends.dto.MatchConfirmedCountDto;
import com.babalegends.entity.Attendance;
import com.babalegends.entity.enums.AttendanceStatus;
import com.babalegends.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    Optional<Attendance> findByMatchIdAndUserId(UUID matchId, UUID userId);

    boolean existsByMatchIdAndUserId(UUID matchId, UUID userId);

    @Query("""
            select a from Attendance a join fetch a.user
            where a.match.id = :matchId and a.status = :status
            order by a.user.name
            """)
    List<Attendance> findAllByMatchIdAndStatusWithUser(UUID matchId, AttendanceStatus status);

    long countByMatchIdAndStatus(UUID matchId, AttendanceStatus status);

    @Query("select a from Attendance a join fetch a.user where a.match.id = :matchId order by a.user.name")
    List<Attendance> findAllByMatchIdWithUser(UUID matchId);

    @Query("""
            select a.match.id from Attendance a
            where a.user.id = :userId
                and a.status = :attendanceStatus
                and a.match.id in (select m.id from Match m
                                   where m.group.id = :groupId and m.status = :matchStatus) 
            """)
    List<UUID> findMatchIdsByUserInGroup(UUID userId, UUID groupId,
                                         AttendanceStatus attendanceStatus, MatchStatus matchStatus);

    @Query("""
            select new com.babalegends.dto.MatchConfirmedCountDto(a.match.id, count(a))
            from Attendance a
            where a.match.id = :matchIds and a.status = :status
            group by a.match.id
            """)
    List<MatchConfirmedCountDto> countByMatchId(Collection<UUID> matchId, AttendanceStatus status);

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from Attendance a
            where a.user.id = :userId
            and a.match.id in (select m.id from Match m where m.group.id = :groupId
                        and m.status = :status)
            """)
    void deleteByUserInGroupMatches(UUID userId, UUID groupId, MatchStatus status);
}
