package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Equipe sorteada com seus respectivos jogadores e média de rating")
public record TeamResponseDto(
        @Schema(description = "Identificador único do time", example = "c1d2e3f4-a5b6-7c8d-9e0f-1a2b3c4d5e6f")
        UUID id,

        @Schema(description = "Nome do time (ex: Time A, Time B)", example = "A")
        String name,

        @Schema(description = "Média de rating/overall dos jogadores da equipe", example = "78.5")
        double averageRating,

        @Schema(description = "Lista de jogadores que compõem a equipe")
        List<TeamPlayerResponseDto> players
) {
}
