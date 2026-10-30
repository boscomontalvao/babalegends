package com.babalegends.dto;

import com.babalegends.entity.Match;
import com.babalegends.entity.enums.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Informações resumidas de uma partida")
public record MatchResponseDto(
        @Schema(description = "Identificador único da partida", example = "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
        UUID id,

        @Schema(description = "Identificador do grupo ao qual a partida pertence", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID groupId,

        @Schema(description = "Data e horário de início", example = "2026-11-15T19:30:00")
        LocalDateTime scheduledAt,

        @Schema(description = "Local da partida", example = "Arena Gol de Placa - Quadra 1")
        String location,

        @Schema(description = "Número total de vagas disponíveis", example = "14")
        Integer slots,

        @Schema(description = "Status atual da partida (SCHEDULED, FINISHED, CANCELLED)", example = "SCHEDULED")
        MatchStatus status,

        @Schema(description = "Quantidade de jogadores com presença confirmada", example = "10")
        long confirmedCount
) {
    public static MatchResponseDto from(Match m, long confirmedCount) {
        return new MatchResponseDto(m.getId(), m.getGroup().getId(), m.getScheduledAt(),
                m.getLocation(), m.getSlots(), m.getStatus(), confirmedCount);
    }
}
