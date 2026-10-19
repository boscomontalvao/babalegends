package com.babalegends.repository;

import com.babalegends.entity.PlayerAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlayerAttributesRepository extends JpaRepository<PlayerAttributes, UUID> {
}
