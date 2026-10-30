package com.babalegends.dto;

import com.babalegends.entity.enums.VoteCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

@Schema(description = "Nota de avaliação para um jogador individual (fornecer 'overall' OU mapa de 'attributes', nunca ambos)")
public record PlayerRatingRequestDto(
        @Schema(description = "Identificador único do jogador sendo avaliado", example = "123e4567-e89b-12d3-a456-426614174000")
        @NotNull(message = "Player id is required.")
        UUID playerId,

        @Schema(description = "Nota geral direta de 0 a 10 (usado na votação simplificada)", example = "8", minimum = "0", maximum = "10")
        @Min(value = 0, message = "Score must be between 0 and 10")
        @Max(value = 10, message = "Score must be between 0 and 10")
        Integer overall,

        @Schema(description = "Notas detalhadas por categoria (PACE, SHOOTING, PASSING, DRIBBLING, DEFENDING, PHYSICALITY), de 0 a 10 cada",
                example = "{\"PACE\": 8, \"SHOOTING\": 7, \"PASSING\": 9, \"DRIBBLING\": 8, \"DEFENDING\": 6, \"PHYSICALITY\": 7}")
        Map<VoteCategory, @NotNull(message = "Score is required.")
        @Min(value = 0, message = "Score must be between 0 and 10")
        @Max(value = 10, message = "Score must be between 0 and 10") Integer> attributes) {

    @JsonIgnore
    @AssertTrue(message = "Provide either 'overall' or 'attributes' for each player, never both")
    public boolean isExactlyOneMode() {
        boolean hasOverall = overall != null;
        boolean hasAttributes = attributes != null && !attributes.isEmpty();
        return hasOverall ^ hasAttributes;
    }
}
