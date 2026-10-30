package com.babalegends.job;

import com.babalegends.service.CardUpdateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;


/**
 * Periodically closes the voting of matches whose voting window has ended,
 * applying their scores to the players' cards.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VotingCloseJob {

    private final CardUpdateService cardUpdateService;

    @Scheduled(fixedDelayString = "${app.voting.close-check-interval-ms:600000}")
    public void closeExpiredVotings() {
        for (UUID matchId : cardUpdateService.findMatchIdsToCloseVoting()) {
            try {
                cardUpdateService.applyVotesToCards(matchId);
            } catch (Exception e) {
                log.error("Failed to apply votes of match {} to cards", matchId, e);
            }
        }
    }
}
