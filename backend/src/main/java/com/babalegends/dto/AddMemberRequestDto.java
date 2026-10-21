package com.babalegends.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddMemberRequestDto(
        @NotBlank(message = "Email should not be empty")
        @Size(max = 150, message = "Email must be at most 150 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email"
        )
        String email
) {
}
