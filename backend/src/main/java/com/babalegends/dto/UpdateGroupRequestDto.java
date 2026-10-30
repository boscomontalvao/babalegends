package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para atualização de informações do grupo")
public record UpdateGroupRequestDto(
        @Schema(description = "Novo nome do grupo", example = "Baba das Lendas F.C.")
        @Size(min = 1, max = 150, message = "Name must have between 1 and 150 characters")
        String name,

        @Schema(description = "Permissão de agendamento de partidas por membros comuns", example = "true")
        Boolean allowMemberScheduling
) {

    @JsonIgnore
    @AssertTrue(message = "Provide at least one field to update")
    public boolean isNameOrAllowMemberSchedulingPresent() {
        return name != null || allowMemberScheduling != null;
    }
}
