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

@RestController
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;
    private final AttendanceService attendanceService;

    @PostMapping("/groups/{groupId}/matches")
    public ResponseEntity<MatchResponseDto> create(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId,
                                                   @Valid @RequestBody CreateMatchRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(principal.getId(), groupId, request));
    }

    @GetMapping("/groups/{groupId}/matches")
    public ResponseEntity<List<MatchResponseDto>> listMatchs(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID groupId,
                                                             @RequestParam(required = false) MatchStatus status) {
        return ResponseEntity.ok(matchService.listGroupMatches(principal.getId(), groupId, status));
    }

    @GetMapping("/matches/{matchId}")
    public ResponseEntity<MatchDetailResponseDto> getMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.getMatch(principal.getId(), matchId));
    }

    @PatchMapping("/matches/{matchId}")
    public ResponseEntity<MatchResponseDto> updateMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId,
                                                        @Valid @RequestBody UpdateMatchRequestDto request) {
        return ResponseEntity.ok(matchService.updateMatch(principal.getId(), matchId, request));
    }

    @PostMapping("/matches/{matchId}/cancel")
    public ResponseEntity<MatchResponseDto> cancelMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.cancelMatch(principal.getId(), matchId));
    }

    @PostMapping("/matches/{matchId}/complete")
    public ResponseEntity<MatchResponseDto> completeMatch(@AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID matchId) {
        return ResponseEntity.ok(matchService.completeMatch(principal.getId(), matchId));
    }

    @PutMapping("/matches/{matchId}/attendance/me")
    public ResponseEntity<MyAttendanceResponseDto> respond(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable UUID matchId,
                                                           @Valid @RequestBody RespondAttendanceRequestDto request) {
        return ResponseEntity.ok(attendanceService.respond(principal.getId(), matchId, request.status()));
    }
}
