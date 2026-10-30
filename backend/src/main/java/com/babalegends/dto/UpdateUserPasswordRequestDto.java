package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração de senha do usuário")
public record UpdateUserPasswordRequestDto(
        @Schema(description = "Senha atual do usuário", example = "senhaAntiga@123")
        @NotBlank(message = "Current password should not be empty")
        String currentPassword,

        @Schema(description = "Nova senha desejada (mínimo de 8 e máximo de 72 caracteres)", example = "novaSenha@123")
        @NotBlank(message = "New password should not be empty")
        @Size(min = 8, max = 72, message = "Password must be between 6 and 72 characters")
        String newPassword
) {
}
