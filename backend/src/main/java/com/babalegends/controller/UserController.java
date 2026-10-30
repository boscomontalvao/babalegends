package com.babalegends.controller;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.dto.UpdateUserPasswordRequestDto;
import com.babalegends.dto.UpdateUserProfileRequestDto;
import com.babalegends.dto.UserResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Usuários", description = "Operações de perfil, alteração de senha, foto de perfil e card de atributos do jogador")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Obter perfil logado", description = "Retorna os dados do perfil do usuário autenticado.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getLoggedUser(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getProfile(principal.getId()));
    }

    @Operation(summary = "Obter meu Card de jogador", description = "Retorna a carta de atributos estilo FIFA/Ultimate Team com overall e métricas do jogador.")
    @GetMapping("/me/card")
    public ResponseEntity<CardResponseDto> getMyCard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.getMyCard(principal.getId()));
    }

    @Operation(summary = "Atualizar nome ou e-mail", description = "Atualiza o nome e/ou e-mail do usuário autenticado (requer senha atual caso altere o e-mail).")
    @PatchMapping("/me")
    public ResponseEntity<UserResponseDto> updateUserName(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestBody @Valid UpdateUserProfileRequestDto updateUserProfileRequestDto) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getId(), updateUserProfileRequestDto));
    }

    @Operation(summary = "Alterar senha", description = "Altera a senha do usuário autenticado validando a senha atual.")
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updateUserPassword(@AuthenticationPrincipal UserPrincipal principal,
                                                   @RequestBody @Valid UpdateUserPasswordRequestDto updateUserPasswordRequestDto) {
        userService.updateUserPassword(principal.getId(),
                updateUserPasswordRequestDto.currentPassword(), updateUserPasswordRequestDto.newPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Atualizar foto de perfil", description = "Faz upload e atualização da foto de perfil do usuário (suporta jpg, png, webp).")
    @PutMapping("/me/picture")
    public ResponseEntity<UserResponseDto> updateUserProfilePicture(@AuthenticationPrincipal UserPrincipal principal,
                                                                    @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(userService.updateUserProfilePicture(principal.getId(), image));
    }

    @Operation(summary = "Remover foto de perfil", description = "Remove a foto de perfil do usuário.")
    @DeleteMapping("/me/picture")
    public ResponseEntity<UserResponseDto> deleteUserProfilePicture(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(userService.removeUserProfilePicture(principal.getId()));
    }
}
