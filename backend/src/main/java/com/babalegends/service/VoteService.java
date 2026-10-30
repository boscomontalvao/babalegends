package com.babalegends.service;

import com.babalegends.dto.CastVotesRequestDto;
import com.babalegends.dto.PlayerRatingRequestDto;
import com.babalegends.dto.VotingStatusResponseDto;
import com.babalegends.entity.Match;
import com.babalegends.entity.User;
import com.babalegends.entity.Vote;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.entity.enums.VoteCategory;
import com.babalegends.exception.BadRequestException;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.ForbiddenException;
import com.babalegends.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final MatchService matchService;
    private final AttendanceService attendanceService;

    @Value("${app.voting.window-hours}")
    private long votingWindowHours;

    @Transactional
    public VotingStatusResponseDto castVotes(UUID requesterId, UUID matchId, CastVotesRequestDto castVotesRequestDto) {
        Match match = matchService.requireMatchAccess(requesterId, matchId);
        ensureVotingIsOpen(match);

        Map<UUID, User> participants = attendanceService.listConfirmedUsers(matchId).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        User voter = participants.get(requesterId);
        if (voter == null) {
            throw new ForbiddenException("Only players who took part in the match can vote.");
        }

        if (voteRepository.existsByMatchIdAndVoterId(matchId, requesterId)) {
            throw new ConflictException("You already voted in this match.");
        }

        Set<UUID> alreadyRated = new HashSet<>();
        List<Vote> votes = new ArrayList<>();
        for (PlayerRatingRequestDto rating : castVotesRequestDto.ratings()) {
            UUID playerId = rating.playerId();
            if (playerId.equals(requesterId)) {
                throw new BadRequestException("You cannot rate yourself");
            }

            User voted = participants.get(playerId);
            if (voted == null) {
                throw new BadRequestException("Player " + playerId + " did not take part in this match");
            }
            if (!alreadyRated.add(playerId)) {
                throw new BadRequestException("Player " + playerId + " appears more than once");
            }

            if (rating.overall() != null) {
                votes.add(newVote(match, voter, voted, VoteCategory.OVERALL, rating.overall()));
            } else {
                rating.attributes().forEach((category, score) -> {
                    if (!category.isAttribute()) {
                        throw new BadRequestException("'" + category + "' is not an attribute; use the 'overall' field");
                    }
                    votes.add(newVote(match, voter, voted, category, score));
                });
            }
        }

        voteRepository.saveAll(votes);
        return new VotingStatusResponseDto(true, true, votingClosesAt(match));
    }

    @Transactional(readOnly = true)
    public VotingStatusResponseDto getMyVotingStatus(UUID requesterId, UUID matchId) {
        Match match = matchService.requireMatchAccess(requesterId, matchId);
        return new VotingStatusResponseDto(
                isMatchOpen(match),
                voteRepository.existsByMatchIdAndVoterId(matchId, requesterId),
                votingClosesAt(match)
        );
    }

    private Vote newVote(Match match, User voter, User voted, VoteCategory category, int score) {
        Vote vote = new Vote();
        vote.setMatch(match);
        vote.setVoter(voter);
        vote.setVoted(voted);
        vote.setCategory(category);
        vote.setScore((short) score);
        return vote;
    }

    private void ensureVotingIsOpen(Match match) {
        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new ConflictException("Voting is only available for completed matches.");
        }
        if (!isMatchOpen(match)) {
            throw new ConflictException("The voting period for this match has ended.");
        }
    }

    private Instant votingClosesAt(Match match) {
        return match.getCompletedAt() == null
                ? null
                : match.getCompletedAt().plus(Duration.ofHours(votingWindowHours));
    }

    private boolean isMatchOpen(Match match) {
        Instant votingCloseAt = votingClosesAt(match);
        return match.getStatus() == MatchStatus.COMPLETED
                && votingCloseAt != null
                && Instant.now().isBefore(votingCloseAt);
    }
}
