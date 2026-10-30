package com.babalegends.domain;

import com.babalegends.entity.enums.VoteCategory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Computes how player cards change after the scores given in a match.
 * Pure class: no Spring and no database.
 * <p/>
 * For each player and each attribute:
 * 1. the scores the player received tell how far above or below the match average they were;
 * 2. that becomes a target value for the attribute (from the attribute scores and/or the overall scores);
 * 3. the attribute moves a fraction (learningRate) of the way towards the target;
 * 4. the new overall is the simple average of the six new attributes.
 */
@Component
public class CardEvolutionCalculator {

    public static final List<VoteCategory> ATTRIBUTES = Arrays.stream(VoteCategory.values())
            .filter(VoteCategory::isAttribute)
            .toList();

    private final int minRaters;
    private final double learningRate;

    public CardEvolutionCalculator(@Value("${app.cards.min-raters:3}") int minRaters,
                                   @Value("${app.cards.learning-rate:0.2}") double learningRate) {
        this.minRaters = minRaters;
        this.learningRate = learningRate;
    }

    /**
     * @param currentCards current attributes (0 to 100, the six attributes only) of each participant
     * @param scores       scores received in the match
     * @return new cards (the six attributes + OVERALL) of each participant
     */
    public Map<UUID, Map<VoteCategory, Integer>> evolve(Map<UUID, Map<VoteCategory, Integer>> currentCards,
                                                        List<Score> scores) {

        Map<VoteCategory, CategoryStats> statsByCategory = buildStatsByCategory(currentCards.keySet(), scores);

        // what the average card of the group looks like
        Map<VoteCategory, Double> groupAverageByAttribute = groupAverageByAttribute(currentCards);
        double groupAverageOverall = currentCards.values().stream()
                .mapToDouble(this::meanOfAttributes)
                .average()
                .orElse(0);

        Map<UUID, Map<VoteCategory, Integer>> newCards = new HashMap<>();
        currentCards.forEach((playerId, currentCard) -> {
            Evidence playerOverallEvidence = statsByCategory.get(VoteCategory.OVERALL).evidenceFor(playerId);
            double overallAdjustment = overallAdjustment(playerOverallEvidence, groupAverageOverall, currentCard);

            Map<VoteCategory, Integer> newCard = new EnumMap<>(VoteCategory.class);
            for (VoteCategory attribute : ATTRIBUTES) {
                Evidence playerAttributeEvidence = statsByCategory.get(attribute).evidenceFor(playerId);
                newCard.put(attribute, newAttributeValue(
                        currentCard.get(attribute),
                        playerOverallEvidence,
                        overallAdjustment,
                        playerAttributeEvidence,
                        groupAverageByAttribute.get(attribute)));
            }
            newCard.put(VoteCategory.OVERALL, clamp(Math.round(meanOfAttributes(newCard))));
            newCards.put(playerId, newCard);
        });
        return newCards;
    }

    private Map<VoteCategory, CategoryStats> buildStatsByCategory(Set<UUID> participants, List<Score> scores) {
        Map<VoteCategory, CategoryStats> statsByCategory = new EnumMap<>(VoteCategory.class);
        for (VoteCategory category : VoteCategory.values()) {
            statsByCategory.put(category, new CategoryStats(scores, category, participants, minRaters));
        }
        return statsByCategory;
    }

    private Map<VoteCategory, Double> groupAverageByAttribute(Map<UUID, Map<VoteCategory, Integer>> currentCards) {
        Map<VoteCategory, Double> averages = new EnumMap<>(VoteCategory.class);
        for (VoteCategory attribute : ATTRIBUTES) {
            averages.put(attribute, currentCards.values().stream()
                    .mapToInt(card -> card.get(attribute))
                    .average()
                    .orElse(0));
        }
        return averages;
    }

    /**
     * How many points the overall scores say should be added to every attribute of the player:
     * (group average + points above/below the match average) - the player's current attribute average.
     * Without valid overall scores, it is 0.
     */
    private double overallAdjustment(Evidence playerOverallEvidence, double groupAverageOverall,
                                     Map<VoteCategory, Integer> currentCard) {

        if (playerOverallEvidence == null) {
            return 0;
        }
        double overallTarget = groupAverageOverall + playerOverallEvidence.pointsFromMatchAverage();
        return overallTarget - meanOfAttributes(currentCard);
    }

    /**
     * New value of one attribute. Blends the targets given by the overall scores and by the attribute
     * scores (weights = number of raters of each kind) and moves a fraction of the way to the blended target.
     */
    private int newAttributeValue(int currentAttributeValue, Evidence playerOverallEvidence, double overallAdjustment,
                                  Evidence playerAttributeEvidence, double groupAverage) {

        int overallRaterCount = raterCount(playerOverallEvidence);
        int attributeRaterCount = raterCount(playerAttributeEvidence);
        if (overallRaterCount + attributeRaterCount == 0) {
            return currentAttributeValue;
        }

        double weightedTargetsSum = 0;
        if (overallRaterCount > 0) {
            // overall scores keep the "shape" of the card and only shift the attribute
            weightedTargetsSum += overallRaterCount * (currentAttributeValue + overallAdjustment);
        }
        if (attributeRaterCount > 0) {
            // attribute scores are anchored on the group average
            weightedTargetsSum += attributeRaterCount
                    * (groupAverage + playerAttributeEvidence.pointsFromMatchAverage());
        }
        double targetValue = weightedTargetsSum / (overallRaterCount + attributeRaterCount);

        return clamp(Math.round(currentAttributeValue + learningRate * (targetValue - currentAttributeValue)));
    }

    private static int raterCount(Evidence evidence) {
        return evidence == null ? 0 : evidence.raterCount();
    }

    private double meanOfAttributes(Map<VoteCategory, Integer> card) {
        return ATTRIBUTES.stream().mapToInt(card::get).average().orElse(0);
    }

    private static int clamp(long value) {
        return (int) Math.max(0, Math.min(100, value));
    }

    /**
     * Summarizes the scores received in one category during the match.
     */
    private static class CategoryStats {

        private final int minRaters;
        private final Map<UUID, IntSummaryStatistics> scoresByPlayer = new HashMap<>();
        private final IntSummaryStatistics allScores = new IntSummaryStatistics();

        CategoryStats(List<Score> scores, VoteCategory category, Set<UUID> participants, int minRaters) {
            this.minRaters = minRaters;
            for (Score score : scores) {
                if (score.category() != category || !participants.contains(score.ratedPlayerId())) {
                    continue;
                }
                scoresByPlayer.computeIfAbsent(score.ratedPlayerId(), id -> new IntSummaryStatistics())
                        .accept(score.score());
                allScores.accept(score.score());
            }
        }

        /**
         * Returns null when there is no reference (fewer than 2 players rated) or not enough raters.
         */
        Evidence evidenceFor(UUID playerId) {
            if (scoresByPlayer.size() < 2) {
                return null;
            }
            IntSummaryStatistics playerScores = scoresByPlayer.get(playerId);
            if (playerScores == null || playerScores.getCount() < minRaters) {
                return null;
            }
            double pointsFromMatchAverage = (playerScores.getAverage() - allScores.getAverage()) * 10;
            return new Evidence((int) playerScores.getCount(), pointsFromMatchAverage);
        }
    }

    /**
     * What the scores of one category say about one player.
     * raterCount: how many people rated the player (used as weight when blending).
     * pointsFromMatchAverage: points above (+) or below (-) the match average, on the 0-100 scale.
     */
    private record Evidence(int raterCount, double pointsFromMatchAverage) {
    }
}