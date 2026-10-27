package com.babalegends.dto;

import com.babalegends.entity.Match;
import com.babalegends.entity.enums.MatchStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record MatchResponseDto(UUID id, UUID groupId, LocalDateTime scheduledAt, String location,
                               Integer slots, MatchStatus status, long confirmedCount) {
    public static MatchResponseDto from(Match m, long confirmedCount) {
        return new MatchResponseDto(m.getId(), m.getGroup().getId(), m.getScheduledAt(),
                m.getLocation(), m.getSlots(), m.getStatus(), confirmedCount);
    }
}
