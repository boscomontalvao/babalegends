package com.babalegends.service;

import com.babalegends.dto.MatchConfirmedCountDto;
import com.babalegends.dto.MyAttendanceResponseDto;
import com.babalegends.entity.Attendance;
import com.babalegends.entity.Match;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.AttendanceStatus;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.exception.BadRequestException;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.repository.AttendanceRepository;
import com.babalegends.repository.MatchRepository;
import com.babalegends.repository.TeamPlayerRepository;
import com.babalegends.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final TeamPlayerRepository teamPlayerRepository;

    /**
     * Set PENDING status to everyone when creating a match
     **/
    @Transactional()
    public void createForMatch(Match match, Collection<User> members) {
        List<Attendance> attendances = members.stream()
                .map(user -> {
                    Attendance attendance = new Attendance();
                    attendance.setMatch(match);
                    attendance.setUser(user);
                    attendance.setStatus(AttendanceStatus.PENDING);
                    attendance.setRespondedAt(null);
                    return attendance;
                }).toList();
        attendanceRepository.saveAll(attendances);
    }

    /**
     * Set PENDING status to a player that joy a group with a match scheduled
     */
    @Transactional
    public void createPendingForUpcomingMatches(UUID groupId, User user) {
        List<Match> scheduledMatches = matchRepository.findByGroupIdAndStatus(groupId, MatchStatus.SCHEDULED);
        for (Match match : scheduledMatches) {
            if (!attendanceRepository.existsByMatchIdAndUserId(match.getId(), user.getId())) {
                Attendance attendance = new Attendance();
                attendance.setMatch(match);
                attendance.setUser(user);
                attendance.setStatus(AttendanceStatus.PENDING);
                attendance.setRespondedAt(null);
                attendanceRepository.save(attendance);
            }
        }
    }

    /**
     * Removes a user from scheduled matches when they leave or are removed from a group. The history is preserved.
     */
    @Transactional
    public void removeFromUpcomingMatches(UUID groupId, UUID userId) {
        List<UUID> confirmedMatchsIds = attendanceRepository.findMatchIdsByUserInGroup(
                userId, groupId, AttendanceStatus.CONFIRMED, MatchStatus.SCHEDULED
        );

        for (UUID matchId : confirmedMatchsIds) {
            matchRepository.findWithLockById(matchId);
        }

        attendanceRepository.deleteByUserInGroupMatches(userId, groupId, MatchStatus.SCHEDULED);

        for (UUID matchId : confirmedMatchsIds) {
            deleteTeams(matchId);
        }
    }

    @Transactional
    public MyAttendanceResponseDto respond(UUID requesterId, UUID matchId, AttendanceStatus newStatus) {
        if (newStatus.equals(AttendanceStatus.PENDING)) {
            throw new BadRequestException("Status must be CONFIRMED or DECLINED.");
        }

        Match match = matchRepository.findWithLockById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));

        Attendance attendance = attendanceRepository.findByMatchIdAndUserId(matchId, requesterId)
                .orElseThrow(() -> new NotFoundException("Match not found."));

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ConflictException("This match is not open for responses.");
        }

        if (newStatus == AttendanceStatus.CONFIRMED
                && attendance.getStatus() != AttendanceStatus.CONFIRMED) {
            long confirmedCases = attendanceRepository.countByMatchIdAndStatus(matchId, AttendanceStatus.CONFIRMED);
            if (confirmedCases >= match.getSlots()) {
                throw new ConflictException("This match is full.");
            }
        }

        AttendanceStatus previosStatus = attendance.getStatus();
        attendance.setStatus(newStatus);
        attendance.setRespondedAt(Instant.now());
        attendanceRepository.save(attendance);

        if ((previosStatus == AttendanceStatus.CONFIRMED) != (newStatus == AttendanceStatus.CONFIRMED)) {
            deleteTeams(matchId);
        }

        return new MyAttendanceResponseDto(matchId, attendance.getStatus(), attendance.getRespondedAt());
    }

    @Transactional(readOnly = true)
    public List<Attendance> listForMatch(UUID matchId) {
        return attendanceRepository.findAllByMatchIdWithUser(matchId);
    }

    @Transactional(readOnly = true)
    public List<User> listConfirmedUsers(UUID matchId) {
        return attendanceRepository.findAllByMatchIdAndStatusWithUser(matchId, AttendanceStatus.CONFIRMED)
                .stream()
                .map(Attendance::getUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public long countMatchConfirmed(UUID matchId) {
        return attendanceRepository.countByMatchIdAndStatus(matchId, AttendanceStatus.CONFIRMED);
    }

    @Transactional(readOnly = true)
    public Map<UUID, Long> countConfirmedByMatchIds(Collection<UUID> matchIds) {
        if (matchIds.isEmpty()) {
            return Map.of();
        }

        return attendanceRepository.countByMatchId(matchIds, AttendanceStatus.CONFIRMED).stream()
                .collect(Collectors.toMap(MatchConfirmedCountDto::matchId, MatchConfirmedCountDto::total));
    }

    private void deleteTeams(UUID matchId) {
        teamPlayerRepository.deleteByMatchId(matchId);
        teamRepository.deleteByMatchId(matchId);
    }
}
