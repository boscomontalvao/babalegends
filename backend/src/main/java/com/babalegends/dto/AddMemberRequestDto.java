package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para adicionar um novo membro ao grupo por e-mail")
public record AddMemberRequestDto(
        @Schema(description = "E-mail do usuário a ser adicionado como membro", example = "amigo@babalegends.com")
        @NotBlank(message = "Email should not be empty")
        @Size(max = 150, message = "Email must be at most 150 characters")
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email"
        )
        String email
) {
}
