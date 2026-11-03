package com.babalegends.mapper;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.infrastructure.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper component for transforming player entities and attributes into card presentation DTOs.
 * <p>
 * Also resolves the prioritized picture URL for a card, favoring a custom card picture
 * if uploaded, and falling back to the user's profile picture.
 */
@RequiredArgsConstructor
@Component
public class CardMapper {

    private final FileStorageService fileStorageService;

    /**
     * Resolves the public URL for the card image.
     * <p>
     * Prefers the dedicated card picture key; if null, falls back to the user's profile picture key.
     *
     * @param cardPictureKey the storage key of the custom card image, or null
     * @param profilePictureKey the storage key of the user's profile image, or null
     * @return the resolved public URL, or null if neither image key is present
     */
    public String getPictureUrl(String cardPictureKey, String profilePictureKey) {
        return fileStorageService.urlFor(cardPictureKey != null ? cardPictureKey : profilePictureKey);
    }

    /**
     * Converts a {@link User} entity and corresponding {@link PlayerAttributes} into a {@link CardResponseDto}.
     *
     * @param user the player user entity
     * @param playerAttributes the player attributes entity containing ratings and card image key
     * @return a populated {@link CardResponseDto} with resolved picture URL and skill attributes
     */
    public CardResponseDto convertAttributesToCardResponse(User user, PlayerAttributes playerAttributes) {
        return new CardResponseDto(
                user.getId(),
                user.getName(),
                getPictureUrl(playerAttributes.getCardPicture(), user.getPicture()),
                playerAttributes.getOverall(),
                playerAttributes.getPace(),
                playerAttributes.getShooting(),
                playerAttributes.getPassing(),
                playerAttributes.getDribbling(),
                playerAttributes.getDefending(),
                playerAttributes.getPhysicality()
        );
    }
}
