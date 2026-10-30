package com.babalegends.service;

import com.babalegends.dto.AttendanceResponseDto;
import com.babalegends.dto.CreateMatchRequestDto;
import com.babalegends.dto.MatchDetailResponseDto;
import com.babalegends.dto.MatchResponseDto;
import com.babalegends.dto.UpdateMatchRequestDto;
import com.babalegends.entity.Attendance;
import com.babalegends.entity.GroupMember;
import com.babalegends.entity.Match;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.AttendanceStatus;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final GroupService groupService;
    private final AttendanceService attendanceService;
    private final UserService userService;

    @Transactional(readOnly = true)
    public MatchDetailResponseDto getMatch(UUID requesterId, UUID matchId) {
        Match match = requireMatchAccess(requesterId, matchId);

        List<Attendance> attendances = attendanceService.listForMatch(matchId);
        long confirmedCount = attendances.stream()
                .filter(attendance -> attendance.getStatus() == AttendanceStatus.CONFIRMED)
                .count();
        AttendanceStatus myStatus = attendances.stream()
                .filter(attendance -> attendance.getUser().getId().equals(requesterId))
                .map(Attendance::getStatus)
                .findFirst()
                .orElse(null);

        return new MatchDetailResponseDto(
                MatchResponseDto.from(match, confirmedCount),
                myStatus,
                attendances.stream().map(a -> AttendanceResponseDto.from(a, userService.getUserPictureUrl(a.getUser()))).toList()
        );
    }

    @Transactional(readOnly = true)
    public List<MatchResponseDto> listGroupMatches(UUID requesterId, UUID groupId, MatchStatus matchStatus) {
        groupService.requireMember(groupId, requesterId);

        List<Match> matches = (matchStatus == null)
                ? matchRepository.findByGroupIdOrderByScheduledAtDesc(groupId)
                : matchRepository.findByGroupIdAndStatus(groupId, matchStatus);

        Map<UUID, Long> confirmedCount = attendanceService.countConfirmedByMatchIds(matches.stream().map(Match::getId).toList());
        return matches.stream()
                .map(match -> MatchResponseDto.from(match, confirmedCount.getOrDefault(match.getId(), 0L)))
                .toList();
    }

    @Transactional
    public MatchResponseDto createMatch(UUID requesterId, UUID groupId, CreateMatchRequestDto createMatchRequestDto) {
        GroupMember me = groupService.requireCanSchedule(groupId, requesterId);

        Match match = new Match();
        match.setGroup(me.getGroup());
        match.setScheduledAt(createMatchRequestDto.scheduledAt());
        match.setLocation(createMatchRequestDto.location());
        match.setSlots(createMatchRequestDto.slots());
        match.setStatus(MatchStatus.SCHEDULED);
        matchRepository.save(match);

        List<User> members = groupService.getMembers(groupId).stream().map(GroupMember::getUser).toList();
        attendanceService.createForMatch(match, members);
        return MatchResponseDto.from(match, 0);
    }

    @Transactional
    public MatchResponseDto updateMatch(UUID requesterId, UUID matchId, UpdateMatchRequestDto updateMatchRequestDto) {
        Match match = loadSchedulableForUpdate(requesterId, matchId);
        ensureScheduled(match);
        long confirmedCount = attendanceService.countMatchConfirmed(matchId);

        if (updateMatchRequestDto.slots() != null) {
            if (updateMatchRequestDto.slots() < confirmedCount) {
                throw new ConflictException("Slots cannot be lower than the number of confirmed players (" + confirmedCount + ")");
            }
            match.setSlots(updateMatchRequestDto.slots());
        }

        if (updateMatchRequestDto.location() != null) {
            match.setLocation(updateMatchRequestDto.location().trim());
        }

        if (updateMatchRequestDto.scheduledAt() != null) {
            match.setScheduledAt(updateMatchRequestDto.scheduledAt());
        }
        matchRepository.save(match);
        return MatchResponseDto.from(match, confirmedCount);
    }

    @Transactional
    public MatchResponseDto cancelMatch(UUID requesterId, UUID matchId) {
        Match match = loadSchedulableForUpdate(requesterId, matchId);
        ensureScheduled(match);
        long confirmedCount = attendanceService.countMatchConfirmed(matchId);
        match.setStatus(MatchStatus.CANCELLED);
        matchRepository.save(match);
        return MatchResponseDto.from(match, confirmedCount);
    }

    @Transactional
    public MatchResponseDto completeMatch(UUID requesterId, UUID matchId) {
        Match match = loadSchedulableForUpdate(requesterId, matchId);
        ensureScheduled(match);
        long confirmedCount = attendanceService.countMatchConfirmed(matchId);
        if (match.getScheduledAt().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Cannot complete a match that doesn't happen;");
        }
        match.setCompletedAt(Instant.now());
        match.setStatus(MatchStatus.COMPLETED);
        return MatchResponseDto.from(match, confirmedCount);
    }

    /**
     * Return 404 if requester user was not in match's groups or if match does not exist.
     *
     **/
    @Transactional(readOnly = true)
    public Match requireMatchAccess(UUID requesterId, UUID matchId) {
        Match match = matchRepository.findByIdWithGroup(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));
        if (!groupService.isMember(match.getGroup().getId(), requesterId)) {
            throw new NotFoundException("Match not found.");
        }

        return match;
    }

    public Match requireMatchForUpdate(UUID requesterId, UUID matchId) {
        Match match = matchRepository.findWithLockById(matchId)
                .orElseThrow(() -> new NotFoundException("Match not found."));

        if (!groupService.isMember(match.getGroup().getId(), requesterId)) {
            throw new NotFoundException("Match not found.");
        }

        return match;
    }

    /**
     * Lock the match and verify whether the match belong to the user group
     * and if the user has permission to schedule.
     *
     * @param requesterId - user id
     * @param matchId     - match id
     * @return The match - {@link Match}
     */
    private Match loadSchedulableForUpdate(UUID requesterId, UUID matchId) {
        Match match = requireMatchForUpdate(requesterId, matchId);
        groupService.requireCanSchedule(match.getGroup().getId(), requesterId);
        return match;
    }

    private void ensureScheduled(Match match) {
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ConflictException("Only scheduled matches can be changed.");
        }
    }
}
