package com.babalegends.service;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.dto.UserOverallDto;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.exception.NotFoundException;
import com.babalegends.infrastructure.FileStorageService;
import com.babalegends.infrastructure.FileTransactionSupport;
import com.babalegends.mapper.CardMapper;
import com.babalegends.repository.PlayerAttributesRepository;
import com.babalegends.util.ImageTypeDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerAttributesService {

    private final PlayerAttributesRepository playerAttributesRepository;
    private final FileStorageService fileStorageService;
    private final FileTransactionSupport fileTransactionSupport;
    private final CardMapper cardMapper;

    @Transactional(readOnly = true)
    public CardResponseDto getMyCard(UUID userId) {
        PlayerAttributes card = getByUserId(userId);
        return cardMapper.convertAttributesToCardResponse(card.getUser(), card);
    }

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
    public Map<UUID, UserOverallDto> getOverallDtoByUserIds(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return playerAttributesRepository.findOverallByUserIds(userIds).stream()
                .collect(Collectors.toMap(UserOverallDto::userId, Function.identity()));
    }

    @Transactional(readOnly = true)
    public Map<UUID, PlayerAttributes> getAttributesByUserIds(Collection<UUID> userIds) {
        return playerAttributesRepository.findByUserIdIn(userIds).stream()
                .collect(Collectors.toMap(playerAttributes -> playerAttributes.getUser().getId(),
                        Function.identity()));
    }

    @Transactional
    public CardResponseDto updateCardPicture(UUID userId, MultipartFile image) {
        String detectedExtension = ImageTypeDetector.detectImageExtension(image);

        PlayerAttributes card = getByUserId(userId);
        User user = card.getUser();

        String oldKey = card.getCardPicture();
        String newKey = fileStorageService.store("card", image, detectedExtension);
        fileTransactionSupport.deleteOnRollback(newKey);

        card.setCardPicture(newKey);
        fileTransactionSupport.deleteAfterCommit(oldKey);

        return cardMapper.convertAttributesToCardResponse(user, card);
    }

    @Transactional
    public CardResponseDto removeCardPicture(UUID userId) {
        PlayerAttributes card = getByUserId(userId);
        String oldKey = card.getCardPicture();
        card.setCardPicture(null);
        fileTransactionSupport.deleteAfterCommit(oldKey);
        return cardMapper.convertAttributesToCardResponse(card.getUser(), card);
    }

    @Transactional
    public String clearCardPicture(UUID userId) {
        PlayerAttributes card = getByUserId(userId);
        String oldKey = card.getCardPicture();
        card.setCardPicture(null);
        return oldKey;
    }
}
