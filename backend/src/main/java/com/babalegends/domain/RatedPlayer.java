package com.babalegends.domain;

import com.babalegends.entity.User;

public record RatedPlayer(User user, int rating) {
}
