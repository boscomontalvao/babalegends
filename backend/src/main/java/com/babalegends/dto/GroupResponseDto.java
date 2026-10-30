package com.babalegends.dto;

import com.babalegends.entity.Group;
import com.babalegends.entity.enums.GroupRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Informações detalhadas do grupo")
public record GroupResponseDto(
        @Schema(description = "Identificador único do grupo", example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d")
        UUID id,

        @Schema(description = "Nome do grupo", example = "Baba dos Amigos de Quarta")
        String name,

        @Schema(description = "Indica se membros comuns têm permissão para agendar partidas", example = "false")
        boolean allowMemberScheduling,

        @Schema(description = "Papel do usuário logado neste grupo", example = "ADMIN")
        GroupRole myRole
) {

    public static GroupResponseDto from(Group g, GroupRole myRole) {
        return new GroupResponseDto(g.getId(), g.getName(), g.isAllowMemberScheduling(), myRole);
    }
}
