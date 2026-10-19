package com.babalegends.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserPasswordRequestDto(
        @NotBlank(message = "Current password should not be empty")
        String currentPassword,

        @NotBlank(message = "New password should not be empty")
        @Size(min = 8, max = 72, message = "Password must be between 6 and 72 characters")
        String newPassword
) {
}
