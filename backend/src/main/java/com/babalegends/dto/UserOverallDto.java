package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Nota geral média (overall) de um jogador")
public record UserOverallDto(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nota geral consolidada do jogador", example = "85")
        Short overall,

        @Schema(description = "Chave da foto personalizada da carta do jogador no armazenamento", example = "cards/123e4567-e89b-12d3-a456-426614174000.jpg")
        String cardPicture
) {
}
