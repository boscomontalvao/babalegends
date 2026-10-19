package com.babalegends.dto;

import com.babalegends.entity.User;

import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String email,
        String picture
) {
    public static UserResponseDto from(User user, String pictureUrl) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail(), pictureUrl);
    }
}
