package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Dados da equipe em que o usuário está escalado na partida, com os cards detalhados dos companheiros")
public record MyTeamResponseDto(
        @Schema(description = "Nome do time (ex: A, B)", example = "A")
        String name,

        @Schema(description = "Média de rating/overall dos jogadores da equipe", example = "82")
        int averageRating,

        @Schema(description = "Lista de cards com atributos estilo FIFA dos jogadores da equipe")
        List<CardResponseDto> myTeamPlayers) {
}
