package com.babalegends.dto;

import com.babalegends.entity.enums.GroupRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleRequestDto(
        @NotNull(message = "Role is required")
        GroupRole role) {
}
