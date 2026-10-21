package com.babalegends.repository;

import com.babalegends.entity.Group;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Group> findWithLockById(UUID uuid);
}
