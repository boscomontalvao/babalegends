package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Schema(description = "Dados para atualização de uma partida agendada")
public record UpdateMatchRequestDto(
        @Schema(description = "Nova data e horário da partida (deve ser futura)", example = "2026-11-16T20:00:00")
        @Future(message = "Date and time must be in the future")
        LocalDateTime scheduledAt,

        @Schema(description = "Novo local da partida", example = "Clube dos Oficiais - Campo Sintético")
        @Size(min = 1, max = 200, message = "Location must have between 1 and 200 characters")
        String location,

        @Schema(description = "Nova quantidade máxima de vagas", example = "16", minimum = "2", maximum = "50")
        @Min(value = 2, message = "Slots must be at least 2")
        @Max(value = 50, message = "Slots must be at most 50")
        Integer slots) {

    @JsonIgnore
    @AssertTrue(message = "Provide at least one field to update")
    public boolean isAnyFieldPresent() {
        return scheduledAt != null || location != null || slots != null;
    }
}
