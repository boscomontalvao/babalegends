package com.babalegends.dto;

import com.babalegends.entity.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para alteração de papel/função de um membro no grupo")
public record ChangeRoleRequestDto(
        @Schema(description = "Novo papel atribuído ao membro", example = "ADMIN")
        @NotNull(message = "Role is required")
        GroupRole role
) {
}
