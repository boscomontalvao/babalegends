package com.babalegends.service;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.dto.UpdateUserProfileRequestDto;
import com.babalegends.dto.UserResponseDto;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.exception.BadRequestException;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.infrastructure.FileStorageService;
import com.babalegends.repository.UserRepository;
import com.babalegends.util.ImageTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlayerAttributesService playerAttributesService;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserResponseDto getProfile(UUID userId) {
        return userToUserResponseDto(getById(userId));
    }

    @Transactional(readOnly = true)
    public User getById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found."));
    }

    @Transactional
    public User createUser(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already registered.");
        }

        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(rawPassword));

        User saved = userRepository.save(user);
        playerAttributesService.createWithDefaultAttributes(saved);
        return saved;
    }

    @Transactional
    public void updateUserPassword(UUID userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(UUID userId, UpdateUserProfileRequestDto updateRequestDto) {
        User user = getById(userId);

        if (updateRequestDto.newName() != null) {
            String name = updateRequestDto.newName().trim();
            if (name.isBlank()) {
                throw new BadRequestException("Name should not be empty");
            }
            user.setName(name);
        }

        if (updateRequestDto.newEmail() != null) {
            String email = updateRequestDto.newEmail().trim().toLowerCase(Locale.ROOT);
            verifyCurrentPassword(user, updateRequestDto.currentPassword());
            if (!email.equals(user.getEmail())) {
                if (userRepository.existsByEmail(email)) {
                    throw new ConflictException("Email already registered.");
                }
                user.setEmail(email);
            }
        }

        userRepository.saveAndFlush(user);
        return userToUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserProfilePicture(UUID userId, MultipartFile image) {
        String detectedImageExtension = ImageTypeDetector.detectImageExtension(image);
        User user = getById(userId);

        String oldKey = user.getPicture();
        String newKey = fileStorageService.store("profile", image, detectedImageExtension);
        deleteOnRollback(newKey);

        user.setPicture(newKey);
        userRepository.save(user);

        deleteAfterCommit(oldKey);
        return userToUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto removeUserProfilePicture(UUID userId) {
        User user = getById(userId);

        String oldKey = user.getPicture();
        user.setPicture(null);
        userRepository.save(user);

        deleteAfterCommit(oldKey);
        return userToUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public CardResponseDto getMyCard(UUID userId) {
        User user = getById(userId);
        PlayerAttributes userAttributes = playerAttributesService.getByUserId(userId);
        return new CardResponseDto(
                user.getId(),
                user.getName(),
                getUserPictureUrl(user),
                userAttributes.getOverall(),
                userAttributes.getPace(),
                userAttributes.getShooting(),
                userAttributes.getPassing(),
                userAttributes.getDribbling(),
                userAttributes.getDefending(),
                userAttributes.getPhysicality()
        );
    }

    public UserResponseDto userToUserResponseDto(User user) {
        return UserResponseDto.from(user, fileStorageService.urlFor(user.getPicture()));
    }

    public String getUserPictureUrl(User user) {
        return user.getPicture() == null ? null : fileStorageService.urlFor(user.getPicture());
    }

    private void deleteAfterCommit(String key) {
        if (key == null) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    fileStorageService.delete(key);
                } catch (RuntimeException e) {
                    log.warn("Could not delete old file {}", key, e);
                }
            }
        });
    }

    private void deleteOnRollback(String key) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    try {
                        fileStorageService.delete(key);
                    } catch (RuntimeException e) {
                        log.warn("Could not clean up file {}", key, e);
                    }
                }
            }
        });
    }

    private void verifyCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BadRequestException("Current password is required");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
    }
}
