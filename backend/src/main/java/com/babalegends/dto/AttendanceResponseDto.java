package com.babalegends.dto;

import com.babalegends.entity.Attendance;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.AttendanceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Resposta de presença de um membro na partida")
public record AttendanceResponseDto(
        @Schema(description = "Identificador único do usuário", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID userId,

        @Schema(description = "Nome do membro", example = "Lucas Silva")
        String name,

        @Schema(description = "URL da foto de perfil", example = "http://localhost:8080/uploads/profiles/123e4567.jpg")
        String picture,

        @Schema(description = "Status da presença (CONFIRMED ou DECLINED)", example = "CONFIRMED")
        AttendanceStatus status,

        @Schema(description = "Data e hora do registro da resposta", example = "2026-11-10T14:22:00Z")
        Instant respondedAt
) {
    public static AttendanceResponseDto from(Attendance attendance, String pictureUrl) {
        User user = attendance.getUser();
        return new AttendanceResponseDto(
                user.getId(), user.getName(), pictureUrl, attendance.getStatus(), attendance.getRespondedAt()
        );
    }
}
