package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Carta estilo FIFA/Ultimate Team com atributos consolidados do jogador")
public record CardResponseDto(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nome do jogador", example = "Neymar Júnior")
        String name,

        @Schema(description = "URL da foto do jogador", example = "http://localhost:8080/uploads/profiles/123e4567.jpg")
        String pictureUrl,

        @Schema(description = "Nota geral calculada (overall)", example = "85")
        int overall,

        @Schema(description = "Atributo Ritmo / Velocidade (Pace)", example = "88")
        int pace,

        @Schema(description = "Atributo Finalização (Shooting)", example = "84")
        int shooting,

        @Schema(description = "Atributo Passe (Passing)", example = "86")
        int passing,

        @Schema(description = "Atributo Drible (Dribbling)", example = "91")
        int dribbling,

        @Schema(description = "Atributo Defesa (Defending)", example = "38")
        int defending,

        @Schema(description = "Atributo Físico (Physicality)", example = "65")
        int physicality) {
}
