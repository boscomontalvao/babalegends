package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de um novo usuário na plataforma")
public record RegisterRequestDto(
        @Schema(description = "Nome completo do usuário", example = "Neymar Júnior")
        @NotBlank(message = "Name should not be empty")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String name,

        @Schema(description = "E-mail único para login", example = "neymar@babalegends.com")
        @NotBlank(message = "Email should not be empty")
        @Size(max = 150, message = "Email must be at most 150 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email"
        )
        String email,

        @Schema(description = "Senha de acesso (mínimo de 8 e máximo de 72 caracteres)", example = "senhaForte@123")
        @NotBlank(message = "Password should not be empty")
        @Size(min = 8, max = 72, message = "Password must be between 6 and 72 characters")
        String password
) {}
