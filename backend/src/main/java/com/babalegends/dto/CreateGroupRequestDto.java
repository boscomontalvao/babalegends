package com.babalegends.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGroupRequestDto(
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must have at most 150 characters")
        String name,

        Boolean allowMemberScheduling
) {
}
