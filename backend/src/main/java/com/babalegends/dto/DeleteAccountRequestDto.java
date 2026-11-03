package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Dados para confirmação de exclusão/desativação da própria conta")
public record DeleteAccountRequestDto(
        @Schema(description = "Senha atual do usuário para confirmação de segurança", example = "senhaForte@123")
        @NotBlank(message = "Password is required.")
        String password
) {
}
