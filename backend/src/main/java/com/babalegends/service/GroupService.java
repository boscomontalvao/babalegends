package com.babalegends.service;

import com.babalegends.dto.AddMemberRequestDto;
import com.babalegends.dto.CreateGroupRequestDto;
import com.babalegends.dto.GroupResponseDto;
import com.babalegends.dto.MemberResponseDto;
import com.babalegends.dto.UpdateGroupRequestDto;
import com.babalegends.entity.Group;
import com.babalegends.entity.GroupMember;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.GroupRole;
import com.babalegends.event.UserDeletionEvent;
import com.babalegends.exception.BadRequestException;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.ForbiddenException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.infrastructure.FileStorageService;
import com.babalegends.repository.GroupMemberRepository;
import com.babalegends.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final UserService userService;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FileStorageService fileStorageService;
    private final AttendanceService attendanceService;

    public boolean isMember(UUID groupId, UUID userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Transactional(readOnly = true)
    public List<GroupMember> getMembers(UUID groupId) {
        return groupMemberRepository.findAllByGroupIdWithUser(groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> listMyGroups(UUID requesterId) {
        return groupMemberRepository.findAllByUserIdWithGroup(requesterId).stream()
                .map(groupMember -> GroupResponseDto.from(groupMember.getGroup(), groupMember.getRole()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> listMyGroupMembers(UUID requesterId, UUID groupId) {
        requireMember(groupId, requesterId);
        return groupMemberRepository.findAllByGroupIdWithUser(groupId).stream()
                .map(member -> MemberResponseDto.from(member, userService.getUserPictureUrl(member.getUser())))
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponseDto getGroup(UUID groupId, UUID userId) {
        GroupMember groupMember = requireMember(groupId, userId);
        return GroupResponseDto.from(groupMember.getGroup(), groupMember.getRole());
    }

    @Transactional
    public GroupResponseDto createGroup(UUID userId, CreateGroupRequestDto createGroupRequestDto) {
        User creator = userService.getById(userId);

        Group group = new Group();
        group.setName(createGroupRequestDto.name());
        group.setAllowMemberScheduling(Boolean.TRUE.equals(createGroupRequestDto.allowMemberScheduling()));
        groupRepository.save(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(creator);
        groupMember.setRole(GroupRole.ADMIN);
        groupMemberRepository.save(groupMember);

        return GroupResponseDto.from(group, GroupRole.ADMIN);
    }

    @Transactional
    public GroupResponseDto updateGroup(UUID requesterId, UUID groupId, UpdateGroupRequestDto updateGroupRequestDto) {
        GroupMember me = requireAdmin(groupId, requesterId);
        Group group = me.getGroup();

        if (updateGroupRequestDto.name() != null) {
            group.setName(updateGroupRequestDto.name());
        }

        if (updateGroupRequestDto.allowMemberScheduling() != null) {
            group.setAllowMemberScheduling(updateGroupRequestDto.allowMemberScheduling());
        }
        groupRepository.save(group);

        return GroupResponseDto.from(group, me.getRole());
    }

    @Transactional
    public MemberResponseDto addMember(UUID requesterId, UUID groupId, AddMemberRequestDto addRequest) {
        GroupMember me = requireAdmin(groupId, requesterId);
        User newUser = userService.getUserByEmail(addRequest.email());

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newUser.getId())) {
            throw new ConflictException("User is already a member of this group.");
        }

        GroupMember membership = new GroupMember();
        membership.setUser(newUser);
        membership.setGroup(me.getGroup());
        membership.setRole(GroupRole.MEMBER);
        groupMemberRepository.save(membership);
        attendanceService.createPendingForUpcomingMatches(groupId, newUser);


        return MemberResponseDto.from(membership, userService.getUserPictureUrl(newUser));
    }

    @Transactional
    public MemberResponseDto changeMemberRole(UUID requesterId,
                                              UUID groupId,
                                              UUID targetUserId,
                                              GroupRole newRole) {
        requireAdmin(groupId, requesterId);
        GroupMember target = findMemberOrThrow(groupId, targetUserId);

        if (target.getRole() == newRole) {
            return MemberResponseDto.from(target, userService.getUserPictureUrl(target.getUser()));
        }

        if (target.getRole() == GroupRole.ADMIN) {
            ensureNotLastAdmin(groupId);
        }

        target.setRole(newRole);
        return MemberResponseDto.from(target, userService.getUserPictureUrl(target.getUser()));
    }


    @EventListener
    @Transactional
    public void onUserDeletion(UserDeletionEvent event) {
        UUID userId = event.userId();

        List<GroupMember> userGroupsMembership = groupMemberRepository.findAllByUserIdWithGroup(userId)
                .stream()
                .sorted(Comparator.comparing(membership -> membership.getGroup().getId()))
                .toList();

        userGroupsMembership.forEach(membership -> lockGroup(membership.getGroup().getId()));

        List<String> blockingGroups = userGroupsMembership.stream()
                .filter(membership -> membership.getRole() == GroupRole.ADMIN)
                .filter(userAdminGroups -> groupMemberRepository.countByGroupIdAndRole(
                        userAdminGroups.getGroup().getId(), GroupRole.ADMIN) == 1)
                .filter(userAdminGroups -> groupMemberRepository.countByGroupId(
                        userAdminGroups.getGroup().getId()) > 1)
                .map(userAdminGroups -> userAdminGroups.getGroup().getName())
                .toList();
        if (!blockingGroups.isEmpty()) {
            throw new ConflictException("You are the only admin of: " + String.join(", ", blockingGroups)
                    + ". Promote another admin in each of them before deleting your account");
        }

        for (GroupMember groupMember : userGroupsMembership) {
            attendanceService.removeFromUpcomingMatches(groupMember.getGroup().getId(),
                    groupMember.getUser().getId());
        }
        groupMemberRepository.deleteAll(userGroupsMembership);
    }

    @Transactional
    public void removeMember(UUID requesterId, UUID groupId, UUID targetUserId) {
        lockGroup(groupId);
        if (requesterId.equals(targetUserId)) {
            requireMember(groupId, requesterId);
        } else {
            requireAdmin(groupId, requesterId);
        }

        GroupMember target = findMemberOrThrow(groupId, targetUserId);

        if (target.getRole() == GroupRole.ADMIN) {
            ensureNotLastAdmin(groupId);
        }

        groupMemberRepository.delete(target);
        attendanceService.removeFromUpcomingMatches(groupId, targetUserId);
    }

    public GroupMember requireCanSchedule(UUID groupId, UUID userId) {
        GroupMember me = requireMember(groupId, userId);

        if (me.getRole() != GroupRole.ADMIN && !me.getGroup().isAllowMemberScheduling()) {
            throw new ForbiddenException("Only group admin can schedule matches in this group.");
        }

        return me;
    }


    public GroupMember requireMember(UUID groupId, UUID userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Group not found."));
    }

    @Transactional(readOnly = true)
    public GroupMember requireAdmin(UUID groupId, UUID userId) {
        GroupMember me = requireMember(groupId, userId);

        if (me.getRole() != GroupRole.ADMIN) {
            throw new ForbiddenException("Only group admins can do this.");
        }

        return me;
    }

    private void lockGroup(UUID groupId) {
        groupRepository.findWithLockById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
    }

    private GroupMember findMemberOrThrow(UUID groupId, UUID userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotFoundException("Member not found in this group"));
    }

    private void ensureNotLastAdmin(UUID groupId) {
        if (groupMemberRepository.countByGroupIdAndRole(groupId, GroupRole.ADMIN) <= 1) {
            throw new BadRequestException("The group must have at least one admin. Assign another admin first.");
        }
    }
}
