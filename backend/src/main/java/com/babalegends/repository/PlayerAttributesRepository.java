package com.babalegends.repository;

import com.babalegends.dto.UserOverallDto;
import com.babalegends.entity.PlayerAttributes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlayerAttributesRepository extends JpaRepository<PlayerAttributes, UUID> {

    @Query("""
            select new com.babalegends.dto.UserOverallDto(pa.user.id, pa.overall)
            from PlayerAttributes pa
            where pa.user.id in :userIds
            """)
    List<UserOverallDto> findOverallByUserIds(Collection<UUID> userIds);
}
