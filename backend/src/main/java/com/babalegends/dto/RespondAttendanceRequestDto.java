package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public record RespondAttendanceRequestDto(
        @NotNull(message = "Status is require.")
        AttendanceStatus status
) {
}
