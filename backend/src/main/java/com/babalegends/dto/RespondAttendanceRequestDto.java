package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para confirmação ou recusa de presença em uma partida")
public record RespondAttendanceRequestDto(
        @Schema(description = "Status de resposta (CONFIRMED ou DECLINED)", example = "CONFIRMED")
        @NotNull(message = "Status is require.")
        AttendanceStatus status
) {
}
