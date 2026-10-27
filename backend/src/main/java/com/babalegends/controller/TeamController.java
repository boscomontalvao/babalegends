package com.babalegends.controller;

import com.babalegends.dto.DrawTeamsRequestDto;
import com.babalegends.dto.TeamResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.TeamService;
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

@RestController
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/matches/{matchId}/teams/draw")
    public ResponseEntity<List<TeamResponseDto>> drawTeams(@AuthenticationPrincipal UserPrincipal principal,
                                                           @PathVariable("matchId") UUID matchId,
                                                           @RequestBody @Valid DrawTeamsRequestDto drawTeamsRequestDto) {
        return ResponseEntity.ok(teamService.drawTeams(principal.getId(), matchId, drawTeamsRequestDto.teamCount()));
    }

    @GetMapping("/matches/{matchId}/teams")
    public ResponseEntity<List<TeamResponseDto>> getTeam(@AuthenticationPrincipal UserPrincipal principal,
                                                         @PathVariable("matchId") UUID matchId) {
        return ResponseEntity.ok(teamService.getTeams(principal.getId(), matchId));
    }
}
