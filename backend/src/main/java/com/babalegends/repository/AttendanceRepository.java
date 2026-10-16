package com.babalegends.repository;

import com.babalegends.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {

    List<Attendance> findByMatchId(UUID matchId);

    Optional<Attendance> findByMatchIdAndUserId(UUID matchId, UUID userId);
}
