package com.babalegends.dto;

import com.babalegends.entity.GroupMember;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Dados de um membro pertencente ao grupo")
public record MemberResponseDto(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nome do membro", example = "Lucas Silva")
        String name,

        @Schema(description = "URL pública da foto de perfil", example = "http://localhost:8080/uploads/profiles/123e4567.jpg")
        String picture,

        @Schema(description = "Papel do membro no grupo", example = "MEMBER")
        GroupRole role
) {
    public static MemberResponseDto from(GroupMember member, String pictureUrl) {
        User user = member.getUser();
        return new MemberResponseDto(user.getId(), user.getName(), pictureUrl, member.getRole());
    }
}
