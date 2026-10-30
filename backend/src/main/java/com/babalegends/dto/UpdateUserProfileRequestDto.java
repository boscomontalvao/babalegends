package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de perfil (nome e/ou e-mail)")
public record UpdateUserProfileRequestDto(
        @Schema(description = "Novo nome do usuário", example = "Neymar Jr.")
        @Size(max = 150, message = "Name must be at most 150 characters")
        String newName,

        @Schema(description = "Novo e-mail do usuário", example = "novoemail@babalegends.com")
        @Pattern(regexp = "^[A-Za-z0-9._%+-]+@([A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$",
                message = "Must be a valid email")
        @Size(max = 150, message = "Email must be at most 150 characters")
        String newEmail,

        @Schema(description = "Senha atual (obrigatória caso o e-mail seja alterado)", example = "senhaForte@123")
        String currentPassword
) {

    @JsonIgnore
    @AssertTrue(message = "At least one field must be provided")
    public boolean isNameOrEmailPresent() {
        return newName != null || newEmail != null;
    }
}
