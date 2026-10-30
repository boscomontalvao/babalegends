package com.babalegends.service;

import com.babalegends.dto.UserOverallDto;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.exception.NotFoundException;
import com.babalegends.repository.PlayerAttributesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerAttributesService {

    private final PlayerAttributesRepository playerAttributesRepository;

    public void createWithDefaultAttributes(User user) {
        PlayerAttributes playerAttributes = new PlayerAttributes();
        playerAttributes.setUser(user);
        playerAttributesRepository.save(playerAttributes);
    }

    @Transactional(readOnly = true)
    public PlayerAttributes getByUserId(UUID userId) {
        return playerAttributesRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Card not found."));
    }

    @Transactional(readOnly = true)
    public Map<UUID, Integer> getOverallByUserIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }

        return playerAttributesRepository.findOverallByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserOverallDto::userId, o -> (int) o.overall()));
    }

    @Transactional(readOnly = true)
    public Map<UUID, PlayerAttributes> getAttributesByUserIds(Collection<UUID> userIds) {
        return playerAttributesRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(playerAttributes -> playerAttributes.getUser().getId(),
                        Function.identity()));
    }
}
