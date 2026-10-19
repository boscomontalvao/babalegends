package com.babalegends.service;

import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.repository.PlayerAttributesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayerAttributesService {

    private final PlayerAttributesRepository playerAttributesRepository;

    public PlayerAttributes createWithDefaultAttributes(User user) {
        PlayerAttributes playerAttributes = new PlayerAttributes();
        playerAttributes.setUser(user);
        return playerAttributesRepository.save(playerAttributes);
    }
}
