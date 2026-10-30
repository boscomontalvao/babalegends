package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Status da janela de votação pós-jogo de uma partida")
public record VotingStatusResponseDto(
        @Schema(description = "Indica se a janela de votação ainda está aberta", example = "true")
        boolean open,

        @Schema(description = "Indica se o usuário logado já enviou seus votos nesta partida", example = "false")
        boolean hasVoted,

        @Schema(description = "Data e hora de encerramento da janela de votação", example = "2026-11-17T21:30:00Z")
        Instant votingClosesAt
) {
}
