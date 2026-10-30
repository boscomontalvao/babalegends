package com.babalegends.dto;

import java.time.Instant;

public record VotingStatusResponseDto(boolean open, boolean hasVoted, Instant votingClosesAt) {
}
