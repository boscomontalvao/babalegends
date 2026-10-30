package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Conjunto de avaliações enviadas para os jogadores de uma partida")
public record CastVotesRequestDto(
        @Schema(description = "Lista de notas atribuídas a cada jogador")
        @NotEmpty(message = "At least one rating is required.")
        @Size(max = 50, message = "Too many ratings.")
        List<@Valid PlayerRatingRequestDto> ratings
) {
}
