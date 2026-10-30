package com.babalegends.service;

import com.babalegends.domain.CardEvolutionCalculator;
import com.babalegends.domain.Score;
import com.babalegends.entity.Match;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.entity.enums.VoteCategory;
import com.babalegends.repository.MatchRepository;
import com.babalegends.repository.VoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardUpdateService {

    private final MatchRepository matchRepository;
    private final VoteRepository voteRepository;
    private final AttendanceService attendanceService;
    private final PlayerAttributesService playerAttributesService;
    private final CardEvolutionCalculator cardEvolutionCalculator;

    @Value("${app.voting.window-hours:48}")
    private long votingWindowHours;

    @Transactional(readOnly = true)
    public List<UUID> findMatchIdsToCloseVoting() {
        Instant completedBefore = Instant.now().minus(Duration.ofHours(votingWindowHours));
        return matchRepository.findMatchIdsToCloseVoting(MatchStatus.COMPLETED, completedBefore);
    }

    @Transactional
    public void applyVotesToCards(UUID matchId) {
        Match match = matchRepository.findWithLockById(matchId).orElse(null);

        if (match == null || match.getStatus() != MatchStatus.COMPLETED || match.getVotesAppliedAt() != null) {
            return;
        }

        List<UUID> participantsIds = attendanceService.listConfirmedUsers(matchId).stream()
                .map(User::getId)
                .toList();

        Map<UUID, PlayerAttributes> cardsByPlayerId = playerAttributesService.getAttributesByUserIds(participantsIds);
        Map<UUID, Map<VoteCategory, Integer>> currentCards = new HashMap<>();
        cardsByPlayerId.forEach((playerId, card) -> currentCards.put(playerId,
                mapAttributesToVoteCategories(card)));
        List<Score> scores = voteRepository.findByMatchId(matchId).stream()
                .map(vote -> new Score(vote.getVoted().getId(), vote.getCategory(), vote.getScore()))
                .toList();

        Map<UUID, Map<VoteCategory, Integer>> newCards = cardEvolutionCalculator.evolve(currentCards, scores);
        newCards.forEach((playerId, newValues) ->
                updateCard(cardsByPlayerId.get(playerId), newValues));

        match.setVotesAppliedAt(Instant.now());
        log.info("Applied {} scores from match {} to {} cards", scores.size(),
                matchId, cardsByPlayerId.size());
    }

    private Map<VoteCategory, Integer> mapAttributesToVoteCategories(PlayerAttributes playerAttributes) {
        Map<VoteCategory, Integer> attributeValues = new EnumMap<>(VoteCategory.class);
        attributeValues.put(VoteCategory.PACE, (int) playerAttributes.getPace());
        attributeValues.put(VoteCategory.SHOOTING, (int) playerAttributes.getShooting());
        attributeValues.put(VoteCategory.PASSING, (int) playerAttributes.getPassing());
        attributeValues.put(VoteCategory.DRIBBLING, (int) playerAttributes.getDribbling());
        attributeValues.put(VoteCategory.DEFENDING, (int) playerAttributes.getDefending());
        attributeValues.put(VoteCategory.PHYSICALITY, (int) playerAttributes.getPhysicality());
        return attributeValues;
    }

    private void updateCard(PlayerAttributes playerAttributes, Map<VoteCategory, Integer> newValues) {
        playerAttributes.setPace(newValues.get(VoteCategory.PACE).shortValue());
        playerAttributes.setShooting(newValues.get(VoteCategory.SHOOTING).shortValue());
        playerAttributes.setPassing(newValues.get(VoteCategory.PASSING).shortValue());
        playerAttributes.setDribbling(newValues.get(VoteCategory.DRIBBLING).shortValue());
        playerAttributes.setDefending(newValues.get(VoteCategory.DEFENDING).shortValue());
        playerAttributes.setPhysicality(newValues.get(VoteCategory.PHYSICALITY).shortValue());
        playerAttributes.setOverall(newValues.get(VoteCategory.OVERALL).shortValue());
    }
}

