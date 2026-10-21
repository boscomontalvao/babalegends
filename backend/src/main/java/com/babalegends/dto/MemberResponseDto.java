package com.babalegends.dto;

import com.babalegends.entity.GroupMember;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.GroupRole;

import java.util.UUID;

public record MemberResponseDto(UUID userId, String name, String picture, GroupRole role) {
    public static MemberResponseDto from(GroupMember member, String pictureUrl) {
        User user = member.getUser();
        return new MemberResponseDto(user.getId(), user.getName(), pictureUrl, member.getRole());
    }
}