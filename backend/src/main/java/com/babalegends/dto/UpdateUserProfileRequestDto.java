package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequestDto(
        @Size(max = 150, message = "Name must be at most 150 characters")
        String newName,

        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String newEmail,

        String currentPassword
) {

    @JsonIgnore
    @AssertTrue(message = "At least one field must be provided")
    public boolean isNameOrEmailPresent() {
        return newName != null || newEmail != null;
    }
}
