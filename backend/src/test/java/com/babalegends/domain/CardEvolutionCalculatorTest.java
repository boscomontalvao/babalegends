package com.babalegends.domain;

import com.babalegends.entity.enums.VoteCategory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.babalegends.entity.enums.VoteCategory.OVERALL;
import static com.babalegends.entity.enums.VoteCategory.PACE;
import static org.assertj.core.api.Assertions.assertThat;

class CardEvolutionCalculatorTest {

    // 3 minimum raters, moves 20% of the way towards the target
    private final CardEvolutionCalculator calculator = new CardEvolutionCalculator(3, 0.2);

    private final UUID a = UUID.randomUUID();
    private final UUID b = UUID.randomUUID();
    private final UUID c = UUID.randomUUID();

    private static Map<VoteCategory, Integer> card(int value) {
        return cardWithPace(value, value);
    }

    private static Map<VoteCategory, Integer> cardWithPace(int pace, int others) {
        Map<VoteCategory, Integer> card = new EnumMap<>(VoteCategory.class);
        for (VoteCategory attribute : CardEvolutionCalculator.ATTRIBUTES) {
            card.put(attribute, others);
        }
        card.put(PACE, pace);
        return card;
    }

    private static List<Score> rate(UUID player, VoteCategory category, int... scores) {
        List<Score> result = new ArrayList<>();
        for (int score : scores) {
            result.add(new Score(player, category, score));
        }
        return result;
    }

    @SafeVarargs
    private static List<Score> all(List<Score>... groups) {
        List<Score> result = new ArrayList<>();
        for (List<Score> group : groups) {
            result.addAll(group);
        }
        return result;
    }

    private static void assertEverything(Map<VoteCategory, Integer> card, int expected) {
        for (VoteCategory category : VoteCategory.values()) {
            assertThat(card.get(category)).as(category.name()).isEqualTo(expected);
        }
    }

    @Test
    void overallVotesMoveAllAttributesRelativeToTheMatchAverage() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(60), c, card(60));
        // match average = 7.0 → A is 1 point below, B 1 above, C on the average
        List<Score> votes = all(
                rate(a, OVERALL, 6, 6, 6),
                rate(b, OVERALL, 8, 7, 9),
                rate(c, OVERALL, 7, 7, 7));

        var result = calculator.evolve(cards, votes);

        assertEverything(result.get(a), 58); // 60 + 0.2 × (50 − 60)
        assertEverything(result.get(b), 62); // 60 + 0.2 × (70 − 60)
        assertEverything(result.get(c), 60);
    }

    @Test
    void attributeVotesOnlyMoveThatAttribute() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(60), c, card(60));
        List<Score> votes = all(
                rate(a, PACE, 5, 5, 5),
                rate(b, PACE, 9, 9, 9),
                rate(c, PACE, 7, 7, 7));

        var result = calculator.evolve(cards, votes);

        assertThat(result.get(b).get(PACE)).isEqualTo(64);       // target 60 + 20 = 80
        assertThat(result.get(b).get(VoteCategory.SHOOTING)).isEqualTo(60);
        assertThat(result.get(b).get(OVERALL)).isEqualTo(61);    // (64 + 5×60) / 6 = 60.67
        assertThat(result.get(a).get(PACE)).isEqualTo(56);
        assertThat(result.get(a).get(OVERALL)).isEqualTo(59);
    }

    @Test
    void overallAndAttributeVotesAreBlendedByNumberOfRaters() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(60), c, card(60));
        List<Score> votes = all(
                rate(a, OVERALL, 6, 6, 6), rate(b, OVERALL, 8, 7, 9), rate(c, OVERALL, 7, 7, 7),
                rate(a, PACE, 5, 5, 5), rate(b, PACE, 9, 9, 9), rate(c, PACE, 7, 7, 7));

        var result = calculator.evolve(cards, votes);

        // B's PACE: targets 70 (overall) and 80 (pace), 3 raters each → 75 → 60 + 0.2 × 15 = 63
        assertThat(result.get(b).get(PACE)).isEqualTo(63);
        // B's other attributes: overall only → target 70 → 62
        assertThat(result.get(b).get(VoteCategory.PASSING)).isEqualTo(62);
        assertThat(result.get(b).get(OVERALL)).isEqualTo(62);
        assertEverything(result.get(c), 60);
    }

    @Test
    void cardAboveGroupPerceptionIsPulledDownEvenWithGoodScore() {
        // PACE: A 60, B 73, C 62 → group average 65. Everyone gets 7 (same score for everyone = match average)
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(
                a, cardWithPace(60, 60), b, cardWithPace(73, 60), c, cardWithPace(62, 60));
        List<Score> votes = all(
                rate(a, PACE, 7, 7, 7), rate(b, PACE, 7, 7, 7), rate(c, PACE, 7, 7, 7));

        var result = calculator.evolve(cards, votes);

        assertThat(result.get(b).get(PACE)).isEqualTo(71); // 73 + 0.2 × (65 − 73) = 71.4
        assertThat(result.get(a).get(PACE)).isEqualTo(61); // moves up towards 65
    }

    @Test
    void playersWithFewerThanMinRatersAreIgnored() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(60), c, card(60));
        List<Score> votes = all(
                rate(a, OVERALL, 6, 6, 6),
                rate(b, OVERALL, 8, 8),          // only 2 raters
                rate(c, OVERALL, 7, 7, 7));

        var result = calculator.evolve(cards, votes);

        assertEverything(result.get(b), 60);
    }

    @Test
    void categoryWithASinglePlayerRatedHasNoReferenceAndChangesNothing() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(60), c, card(60));

        var result = calculator.evolve(cards, rate(b, OVERALL, 9, 9, 9));

        assertEverything(result.get(b), 60);
    }

    @Test
    void valuesStayBetweenZeroAndOneHundred() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(
                a, card(60), b, cardWithPace(99, 60), c, card(60));
        List<Score> votes = all(
                rate(a, PACE, 0, 0, 0), rate(b, PACE, 10, 10, 10), rate(c, PACE, 0, 0, 0));

        var result = calculator.evolve(cards, votes);

        assertThat(result.get(b).get(PACE)).isEqualTo(100);
        assertThat(result.get(a).get(PACE)).isBetween(0, 100);
    }

    @Test
    void withoutVotesNothingChanges() {
        Map<UUID, Map<VoteCategory, Integer>> cards = Map.of(a, card(60), b, card(70), c, card(55));

        var result = calculator.evolve(cards, List.of());

        assertEverything(result.get(a), 60);
        assertEverything(result.get(b), 70);
        assertEverything(result.get(c), 55);
    }
}