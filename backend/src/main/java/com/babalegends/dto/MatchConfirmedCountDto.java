package com.babalegends.dto;

import java.util.UUID;

public record MatchConfirmedCountDto(UUID matchId, long total) {
}
