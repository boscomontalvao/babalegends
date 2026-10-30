package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Parâmetros para sorteio e balanceamento de equipes")
public record DrawTeamsRequestDto(
        @Schema(description = "Número de equipes a serem formadas (mínimo 2)", example = "2", minimum = "2")
        @NotNull(message = "Team count is required")
        @Min(value = 2, message = "At least 2 teams is required")
        Integer teamCount
) {
}
