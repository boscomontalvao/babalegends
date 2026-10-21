package com.babalegends.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record UpdateGroupRequestDto(
        @Size(min = 1, max = 150, message = "Name must have between 1 and 150 characters")
        String name,

        Boolean allowMemberScheduling
) {

    @JsonIgnore
    @AssertTrue(message = "Provide at least one field to update")
    public boolean isNameOrAllowMemberSchedulingPresent() {
        return name != null || allowMemberScheduling != null;

    }
}