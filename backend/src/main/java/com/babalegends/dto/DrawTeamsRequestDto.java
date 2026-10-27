package com.babalegends.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DrawTeamsRequestDto(
        @NotNull(message = "Team count is required")
        @Min(value = 2, message = "At least 2 teams is required")
        Integer teamCount
) {
}
