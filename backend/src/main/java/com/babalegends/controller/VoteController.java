package com.babalegends.controller;

import com.babalegends.dto.CastVotesRequestDto;
import com.babalegends.dto.VotingStatusResponseDto;
import com.babalegends.security.UserPrincipal;
import com.babalegends.service.VoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class VoteController {

    private final VoteService voteService;

    @PostMapping("matches/{matchId}/votes")
    public ResponseEntity<VotingStatusResponseDto> castVotes(@AuthenticationPrincipal UserPrincipal principal,
                                                             @PathVariable UUID matchId,
                                                             @RequestBody @Valid CastVotesRequestDto castVotesRequestDto) {
        return ResponseEntity.ok(voteService.castVotes(principal.getId(), matchId, castVotesRequestDto));
    }

    @GetMapping("matches/{matchId}/votes/me")
    public ResponseEntity<VotingStatusResponseDto> getMyVotingStatus(@AuthenticationPrincipal UserPrincipal principal,
                                                                     @PathVariable UUID matchId) {
        return ResponseEntity.ok(voteService.getMyVotingStatus(principal.getId(), matchId));
    }
}
