package com.babalegends.dto;

import com.babalegends.entity.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detalhes completos de uma partida incluindo lista de presenças e status do usuário logado")
public record MatchDetailResponseDto(
        @Schema(description = "Dados da partida")
        MatchResponseDto match,

        @Schema(description = "Status de presença do usuário logado na partida", example = "CONFIRMED")
        AttendanceStatus myStatus,

        @Schema(description = "Lista de confirmações/respostas dos membros")
        List<AttendanceResponseDto> attendances
) {
}
