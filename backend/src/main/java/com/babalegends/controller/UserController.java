package com.babalegends.controller;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.dto.UpdateUserPasswordRequestDto;
import com.babalegends.dto.UpdateUserProfileRequestDto;
import com.babalegends.dto.UserResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getLoggedUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @GetMapping("/me/card")
    public ResponseEntity<CardResponseDto> getMyCard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getMyCard(principal.getId()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateUserName(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestBody @Valid UpdateUserProfileRequestDto updateUserProfileRequestDto) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getId(), updateUserProfileRequestDto));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody @Valid UpdateUserPasswordRequestDto updateUserPasswordRequestDto) {
        userService.updateUserPassword(principal.getId(),
                updateUserPasswordRequestDto.currentPassword(), updateUserPasswordRequestDto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/picture")
    public ResponseEntity<UserResponseDto> updateUserProfilePicture(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(userService.updateUserProfilePicture(principal.getId(), image));
    }

    @DeleteMapping("/me/picture")
    public ResponseEntity<UserResponseDto> deleteUserProfilePicture(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.removeUserProfilePicture(principal.getId()));
    }
}
