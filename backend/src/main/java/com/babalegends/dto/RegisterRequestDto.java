package com.babalegends.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequestDto(
        @NotBlank(message = "Name should not be empty")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @NotBlank(message = "Email should not be empty")
        @Size(max = 150, message = "Email must be at most 150 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email"
        )
        String email,

        @NotBlank(message = "Password should not be empty")
        @Size(min = 8, max = 72, message = "Password must be between 6 and 72 characters")
        String password
) {}
