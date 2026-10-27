package com.babalegends.repository;

import com.babalegends.entity.GroupMember;
import com.babalegends.entity.enums.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    long countByGroupIdAndRole(UUID groupId, GroupRole role);

    @Query("select gm from GroupMember gm join fetch gm.group where gm.user.id = :userId order by gm.group.name")
    List<GroupMember> findAllByUserIdWithGroup(UUID userId);

    @Query("select gm from GroupMember gm join fetch gm.user where gm.group.id = :groupId order by gm.group.name")
    List<GroupMember> findAllByGroupIdWithUser(UUID groupId);
}
