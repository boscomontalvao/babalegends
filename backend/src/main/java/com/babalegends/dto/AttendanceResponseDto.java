package com.babalegends.dto;

import com.babalegends.entity.Attendance;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.AttendanceStatus;

import java.time.Instant;
import java.util.UUID;

public record AttendanceResponseDto(UUID userId, String name, String picture,
                                    AttendanceStatus status, Instant respondedAt) {
    public static AttendanceResponseDto from(Attendance attendance, String pictureUrl) {
        User user = attendance.getUser();
        return new AttendanceResponseDto(
                user.getId(), user.getName(), pictureUrl, attendance.getStatus(), attendance.getRespondedAt()
        );
    }
}
