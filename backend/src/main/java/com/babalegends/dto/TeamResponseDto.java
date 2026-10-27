package com.babalegends.dto;

import java.util.List;
import java.util.UUID;

public record TeamResponseDto(UUID id,
                              String name,
                              double averageRating,
                              List<TeamPlayerResponseDto> players) {
}
