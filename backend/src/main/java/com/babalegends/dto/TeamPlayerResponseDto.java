package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados de um jogador escalado em uma equipe")
public record TeamPlayerResponseDto(
        @Schema(description = "Identificador único do jogador", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nome do jogador", example = "Neymar Júnior")
        String name,

        @Schema(description = "URL da foto de perfil do jogador", example = "http://localhost:8080/uploads/profiles/123e4567.jpg")
        String pictureUrl,

        @Schema(description = "Rating/overall do jogador utilizado no sorteio", example = "85")
        int rating
) {
}
