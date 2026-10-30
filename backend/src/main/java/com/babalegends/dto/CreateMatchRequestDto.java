package com.babalegends.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados para criação e agendamento de uma nova partida")
public record CreateMatchRequestDto(
        @Schema(description = "Data e horário de início da partida (deve ser futura)", example = "2026-11-15T19:30:00")
        @NotNull(message = "Date and time are required")
        @Future(message = "Date and time must be in the future")
        LocalDateTime scheduledAt,

        @Schema(description = "Local onde ocorrerá a partida", example = "Arena Gol de Placa - Quadra 1")
        @NotBlank(message = "Location is required")
        @Size(max = 200, message = "Location must have at most 200 characters")
        String location,

        @Schema(description = "Número máximo de vagas disponíveis para confirmação", example = "14", minimum = "2", maximum = "50")
        @NotNull(message = "Slots is required")
        @Min(value = 2, message = "Slots must be at least 2")
        @Max(value = 50, message = "Slots must be at most 50")
        Integer slots) {
}
