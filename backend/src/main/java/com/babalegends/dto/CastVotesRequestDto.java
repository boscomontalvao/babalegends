package com.babalegends.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CastVotesRequestDto(
        @NotEmpty(message = "At least one rating is required.")
        @Size(max = 50, message = "Too many ratings.")
        List<@Valid PlayerRatingRequestDto> ratings
) {
}
