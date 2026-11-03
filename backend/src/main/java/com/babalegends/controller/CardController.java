package com.babalegends.controller;

import com.babalegends.dto.CardResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.PlayerAttributesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Cartas", description = "Operações relacionadas à carta/card do jogador e foto personalizada da carta")
@RestController
@RequestMapping("/users/me/card")
@RequiredArgsConstructor
public class CardController {

    private final PlayerAttributesService playerAttributesService;

    @Operation(summary = "Obter meu Card de jogador", description = "Retorna a carta de atributos estilo FIFA/Ultimate Team com overall e métricas do jogador.")
    @GetMapping
    public ResponseEntity<CardResponseDto> getMyCard(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(playerAttributesService.getMyCard(principal.getId()));
    }

    @Operation(
            summary = "Atualizar foto da carta",
            description = "Faz upload da foto exibida na carta do jogador (suporta os mesmos limites de tipo e tamanho da foto de perfil). Caso não haja foto personalizada da carta, será utilizada a foto do perfil.")
    @PutMapping("/picture")
    public ResponseEntity<CardResponseDto> updateCardPicture(@AuthenticationPrincipal UserPrincipal principal,
                                                             @RequestParam("image") MultipartFile image) {
        return ResponseEntity.ok(playerAttributesService.updateCardPicture(principal.getId(), image));
    }

    @Operation(
            summary = "Remover foto da carta",
            description = "Remove a foto personalizada da carta do jogador, fazendo com que ela volte a utilizar a foto de perfil.")
    @DeleteMapping("/picture")
    public ResponseEntity<CardResponseDto> removeCardPicture(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(playerAttributesService.removeCardPicture(principal.getId()));
    }
}
