package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record UpdateMatchRequestDto(
        @Future(message = "Date and time must be in the future")
        LocalDateTime scheduledAt,

        @Size(min = 1, max = 200, message = "Location must have between 1 and 200 characters")
        String location,

        @Min(value = 2, message = "Slots must be at least 2")
        @Max(value = 50, message = "Slots must be at most 50")
        Integer slots) {

    @JsonIgnore
    @AssertTrue(message = "Provide at least one field to update")
    public boolean isAnyFieldPresent() {
        return scheduledAt != null || location != null || slots != null;
    }
}
