package com.babalegends.controller;

import com.babalegends.dto.AddMemberRequestDto;
import com.babalegends.dto.ChangeRoleRequestDto;
import com.babalegends.dto.CreateGroupRequestDto;
import com.babalegends.dto.GroupResponseDto;
import com.babalegends.dto.MemberResponseDto;
import com.babalegends.dto.UpdateGroupRequestDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @GetMapping
    public ResponseEntity<List<GroupResponseDto>> listMine(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(groupService.listMyGroups(principal.getId()));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroup(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId, principal.getId()));
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<MemberResponseDto>> listMembers(@AuthenticationPrincipal UserPrincipal principal,
                                                               @PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.listGroupMembers(principal.getId(), groupId));
    }

    @PostMapping
    public ResponseEntity<GroupResponseDto> create(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody @Valid CreateGroupRequestDto createGroupRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(principal.getId(), createGroupRequestDto));
    }

    @PatchMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> update(@AuthenticationPrincipal UserPrincipal principal,
                                                   @PathVariable UUID groupId,
                                                   @RequestBody @Valid UpdateGroupRequestDto updateGroupRequestDto) {
        return ResponseEntity.ok(groupService.updateGroup(principal.getId(), groupId, updateGroupRequestDto));
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<MemberResponseDto> addMember(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable UUID groupId,
                                                       @RequestBody @Valid AddMemberRequestDto addMemberRequestDto) {
        return ResponseEntity.ok(groupService.addMember(principal.getId(), groupId, addMemberRequestDto));
    }

    @PatchMapping("/{groupId}/members/{userId}")
    public ResponseEntity<MemberResponseDto> changeUserRole(@AuthenticationPrincipal UserPrincipal principal,
                                                            @PathVariable UUID groupId, @PathVariable UUID userId,
                                                            @RequestBody @Valid ChangeRoleRequestDto changeRoleRequestDto) {
        return ResponseEntity.ok(groupService.changeMemberRole(
                principal.getId(), groupId, userId, changeRoleRequestDto.role())
        );
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeMember(@AuthenticationPrincipal UserPrincipal principal,
                                             @PathVariable UUID groupId, @PathVariable UUID userId) {
        groupService.removeMember(principal.getId(), groupId, userId);
        return ResponseEntity.noContent().build();
    }
}
