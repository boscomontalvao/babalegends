package com.babalegends.dto;

import java.util.UUID;

public record CardResponseDto(
        UUID userId,
        String name,
        String pictureUrl,
        int overall,
        int pace,
        int shooting,
        int passing,
        int dribbling,
        int defending,
        int physicality) {
}
