package com.babalegends.controller;

import com.babalegends.dto.DrawTeamsRequestDto;
import com.babalegends.dto.MyTeamResponseDto;
import com.babalegends.dto.TeamResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Times", description = "Sorteio e balanceamento inteligente de equipes para partidas")
@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Sortear e balancear times", description = "Realiza o sorteio e distribuição equilibrada dos jogadores confirmados em equipes usando o algoritmo TeamBalancer.")
    @PostMapping("/matches/{matchId}/teams/draw")
    public ResponseEntity<List<TeamResponseDto>> drawTeams(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable("matchId") UUID matchId,
                                                           @RequestBody @Valid DrawTeamsRequestDto drawTeamsRequestDto) {
        return ResponseEntity.ok(teamService.drawTeams(principal.getId(), matchId, drawTeamsRequestDto.teamCount()));
    }

    @Operation(summary = "Obter times sorteados da partida", description = "Retorna a lista de times e jogadores já sorteados para a partida informada.")
    @GetMapping("/matches/{matchId}/teams")
    public ResponseEntity<List<TeamResponseDto>> getTeam(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable("matchId") UUID matchId) {
        return ResponseEntity.ok(teamService.getTeams(principal.getId(), matchId));
    }

    @Operation(summary = "Obter meu time na partida", description = "Retorna os dados da equipe na qual o usuário autenticado foi escalado, acompanhado dos cards detalhados de atributos de cada jogador.")
    @GetMapping("/matches/{matchId}/teams/me")
    public ResponseEntity<MyTeamResponseDto> getMyTeam(@AuthenticationPrincipal UserPrincipal principal,
                                                       @PathVariable("matchId") UUID matchId) {
        return ResponseEntity.ok(teamService.getMyTeam(principal.getId(), matchId));
    }
}
