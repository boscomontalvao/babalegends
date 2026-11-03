package com.babalegends.service;

import com.babalegends.dto.DeleteAccountRequestDto;
import com.babalegends.dto.UpdateUserProfileRequestDto;
import com.babalegends.dto.UserResponseDto;
import com.babalegends.entity.User;
import com.babalegends.event.UserDeletionEvent;
import com.babalegends.exception.BadRequestException;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.infrastructure.FileStorageService;
import com.babalegends.infrastructure.FileTransactionSupport;
import com.babalegends.mapper.CardMapper;
import com.babalegends.repository.UserRepository;
import com.babalegends.util.ImageTypeDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
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
    private final FileTransactionSupport fileTransactionSupport;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CardMapper cardMapper;

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

    @Transactional(readOnly = true)
    public User getActiveUserById(UUID userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getDeletedAt() == null)
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
        fileTransactionSupport.deleteOnRollback(newKey);

        user.setPicture(newKey);
        userRepository.save(user);

        fileTransactionSupport.deleteAfterCommit(oldKey);
        return userToUserResponseDto(user);
    }

    @Transactional
    public UserResponseDto removeUserProfilePicture(UUID userId) {
        User user = getById(userId);

        String oldKey = user.getPicture();
        user.setPicture(null);
        userRepository.save(user);

        fileTransactionSupport.deleteAfterCommit(oldKey);
        return userToUserResponseDto(user);
    }

    @Transactional
    public void deleteMyAccount(UUID userId, DeleteAccountRequestDto deleteAccountRequestDto) {
        User user = getById(userId);

        if (!passwordEncoder.matches(deleteAccountRequestDto.password(), user.getPassword())) {
            throw new BadRequestException("Password is incorrect.");
        }

        applicationEventPublisher.publishEvent(new UserDeletionEvent(userId));

        String cardPictureKey = playerAttributesService.clearCardPicture(userId);
        String userPicture = user.getPicture();
        user.setEmail("delete-" + user.getId() + "@deleted.invalid");
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setPicture(null);
        user.setDeletedAt(Instant.now());

        fileTransactionSupport.deleteAfterCommit(cardPictureKey);
        fileTransactionSupport.deleteAfterCommit(userPicture);
    }

    public UserResponseDto userToUserResponseDto(User user) {
        return UserResponseDto.from(user, fileStorageService.urlFor(user.getPicture()));
    }

    public String getUserPictureUrl(User user) {
        return user.getPicture() == null ? null : fileStorageService.urlFor(user.getPicture());
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
