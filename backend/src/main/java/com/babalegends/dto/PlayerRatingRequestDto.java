package com.babalegends.dto;

import com.babalegends.entity.enums.VoteCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record PlayerRatingRequestDto(
        @NotNull(message = "Player id is required.")
        UUID playerId,

        @Min(value = 0, message = "Score must be between 0 and 10")
        @Max(value = 10, message = "Score must be between 0 and 10")
        Integer overall,

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
