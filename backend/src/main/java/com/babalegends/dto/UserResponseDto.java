package com.babalegends.dto;

import com.babalegends.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados de retorno do perfil do usuário")
public record UserResponseDto(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Nome do usuário", example = "Neymar Júnior")
        String name,

        @Schema(description = "E-mail do usuário", example = "neymar@babalegends.com")
        String email,

        @Schema(description = "URL pública da foto de perfil", example = "http://localhost:8080/uploads/profiles/123e4567.jpg")
        String picture
) {
    public static UserResponseDto from(User user, String pictureUrl) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), pictureUrl);
    }
}
