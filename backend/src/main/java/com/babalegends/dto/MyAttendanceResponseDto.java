package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;

import java.time.Instant;
import java.util.UUID;

public record MyAttendanceResponseDto(UUID matchId, AttendanceStatus status, Instant respondedAt) {
}