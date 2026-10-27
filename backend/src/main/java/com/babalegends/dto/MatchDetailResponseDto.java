package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;

import java.util.List;

public record MatchDetailResponseDto(MatchResponseDto match, AttendanceStatus myStatus,
                                     List<AttendanceResponseDto> attendances) {
}
