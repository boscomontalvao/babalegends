package com.babalegends.controller;

import com.babalegends.dto.CreateMatchRequestDto;
import com.babalegends.dto.MatchDetailResponseDto;
import com.babalegends.dto.MatchResponseDto;
import com.babalegends.dto.MyAttendanceResponseDto;
import com.babalegends.dto.RespondAttendanceRequestDto;
import com.babalegends.dto.UpdateMatchRequestDto;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.AttendanceService;
import com.babalegends.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Tag(name = "Partidas", description = "Agendamento, consulta, atualização, cancelamento e confirmação de presença em partidas")
@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final AttendanceService attendanceService;

    @Operation(summary = "Criar nova partida", description = "Agenda uma nova partida vinculada a um grupo.")
    @PostMapping("/groups/{groupId}/matches")
    public ResponseEntity<MatchResponseDto> create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId,
                                                   @Valid @RequestBody CreateMatchRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(principal.getId(), groupId, request));
    }

    @Operation(summary = "Listar partidas do grupo", description = "Retorna a lista de partidas agendadas para o grupo, opcionalmente filtrada por status.")
    @GetMapping("/groups/{groupId}/matches")
    public ResponseEntity<List<MatchResponseDto>> listMatchs(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId,
                                                             @RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(matchService.listGroupMatches(principal.getId(), groupId, status));
    }

    @Operation(summary = "Buscar detalhes da partida", description = "Retorna dados da partida, status do usuário autenticado e lista de respostas de presença.")
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchDetailResponseDto> getMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.getMatch(principal.getId(), matchId));
    }

    @Operation(summary = "Atualizar partida", description = "Altera local, horário ou vagas de uma partida agendada.")
    @PatchMapping("/matches/{matchId}")
    public ResponseEntity<MatchResponseDto> updateMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId,
                                                        @Valid @RequestBody UpdateMatchRequestDto request) {
        return ResponseEntity.ok(matchService.updateMatch(principal.getId(), matchId, request));
    }

    @Operation(summary = "Cancelar partida", description = "Cancela uma partida agendada.")
    @PostMapping("/matches/{matchId}/cancel")
    public ResponseEntity<MatchResponseDto> cancelMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.cancelMatch(principal.getId(), matchId));
    }

    @Operation(summary = "Concluir partida", description = "Finaliza uma partida e abre a janela de votação pós-jogo para avaliação dos jogadores.")
    @PostMapping("/matches/{matchId}/complete")
    public ResponseEntity<MatchResponseDto> completeMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.completeMatch(principal.getId(), matchId));
    }

    @Operation(summary = "Responder presença na partida", description = "Confirma (CONFIRMED) ou recusa (DECLINED) a presença do usuário na partida.")
    @PutMapping("/matches/{matchId}/attendance/me")
    public ResponseEntity<MyAttendanceResponseDto> respond(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable UUID matchId,
                                                           @Valid @RequestBody RespondAttendanceRequestDto request) {
        return ResponseEntity.ok(attendanceService.respond(principal.getId(), matchId, request.status()));
    }
}
