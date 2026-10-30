package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Contagem total de confirmações de uma partida")
public record MatchConfirmedCountDto(
        @Schema(description = "Identificador da partida", example = "b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d6e")
        UUID matchId,

        @Schema(description = "Total de membros confirmados", example = "12")
        long total
) {
}
