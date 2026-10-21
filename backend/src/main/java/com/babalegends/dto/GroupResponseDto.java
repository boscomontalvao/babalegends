package com.babalegends.dto;

import com.babalegends.entity.Group;
import com.babalegends.entity.enums.GroupRole;

import java.util.UUID;

public record GroupResponseDto(UUID id, String name, boolean allowMemberScheduling, GroupRole myRole) {

    public static GroupResponseDto from(Group g, GroupRole myRole) {
        return new GroupResponseDto(g.getId(), g.getName(), g.isAllowMemberScheduling(), myRole);
    }
}
