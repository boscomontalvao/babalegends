package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação de um novo grupo/pelada")
public record CreateGroupRequestDto(
        @Schema(description = "Nome do grupo", example = "Baba dos Amigos de Quarta")
        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must have at most 150 characters")
        String name,

        @Schema(description = "Indica se membros comuns podem agendar partidas ou apenas administradores", example = "false", defaultValue = "false")
        Boolean allowMemberScheduling
) {
}
