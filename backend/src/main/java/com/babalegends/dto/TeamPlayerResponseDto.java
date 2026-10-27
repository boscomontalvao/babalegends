package com.babalegends.dto;

import java.util.UUID;

public record TeamPlayerResponseDto(UUID userId, String name, String pictureUrl, int rating) {
}
