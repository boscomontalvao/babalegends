package com.babalegends.domain;

import com.babalegends.entity.enums.VoteCategory;

import java.util.UUID;

public record Score(UUID ratedPlayerId, VoteCategory category, int score) {
}
