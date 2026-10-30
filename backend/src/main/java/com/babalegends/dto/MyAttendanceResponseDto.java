package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Status de presença do usuário logado em uma partida específica")
public record MyAttendanceResponseDto(
        @Schema(description = "Identificador da partida", example = "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
        UUID matchId,

        @Schema(description = "Status de presença atual", example = "CONFIRMED")
        AttendanceStatus status,

        @Schema(description = "Momento da resposta", example = "2026-11-10T14:22:00Z")
        Instant respondedAt
) {
}
