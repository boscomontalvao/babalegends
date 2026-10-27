package com.babalegends.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateMatchRequestDto(
        @NotNull(message = "Date and time are required")
        @Future(message = "Date and time must be in the future")
        LocalDateTime scheduledAt,

        @NotBlank(message = "Location is required")
        @Size(max = 200, message = "Location must have at most 200 characters")
        String location,

        @NotNull(message = "Slots is required")
        @Min(value = 2, message = "Slots must be at least 2")
        @Max(value = 50, message = "Slots must be at most 50")
        Integer slots) {
}
