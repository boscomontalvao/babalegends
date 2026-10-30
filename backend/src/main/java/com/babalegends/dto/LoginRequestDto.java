package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para autenticação e obtenção de token JWT")
public record LoginRequestDto(
        @Schema(description = "E-mail do usuário cadastrado", example = "ana@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,

        @Schema(description = "Senha do usuário", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
        String password
) {
}
