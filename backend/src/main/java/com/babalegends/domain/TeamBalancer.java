package com.babalegends.domain;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Random;

/**
 * Service component responsible for balancing and distributing players into teams.
 * <p>
 * Uses a combination of rating-based tier shuffling and serpentine (snake) drafting,
 * evaluated across multiple attempts to find configurations with a minimal rating spread
 * between teams.
 */
@Component
public class TeamBalancer {

    private static final int ATTEMPTS = 100;
    private static final double TOLERANCE = 1.0;

    private final Random random;

    /**
     * Constructs a new {@code TeamBalancer} using a default {@link Random} instance.
     */
    public TeamBalancer() {
        this(new Random());
    }

    /**
     * Constructs a new {@code TeamBalancer} with the specified {@link Random} instance.
     * Primarily used for deterministic testing.
     *
     * @param random the random generator instance to use for shuffling and selection
     */
    TeamBalancer(Random random) {
        this.random = random;
    }

    /**
     * Balances and draws teams from the given list of rated players.
     * <p>
     * Players are sorted by rating in descending order. Multiple balancing attempts are generated
     * using tiered shuffling followed by serpentine distribution. One configuration is randomly
     * selected from the candidate attempts that fall within an acceptable spread tolerance
     * of the best observed rating spread.
     *
     * @param ratedPlayers the list of players with their respective ratings
     * @param teamCount    the number of teams to form
     * @return a list of teams, where each team is represented as a list of {@link RatedPlayer}
     */
    public List<List<RatedPlayer>> draw(List<RatedPlayer> ratedPlayers, int teamCount) {
        if (teamCount < 2) {
            throw new IllegalArgumentException("teamCount must be at least 2");
        }
        if (ratedPlayers.size() < teamCount) {
            throw new IllegalArgumentException("Not enough players for " + teamCount + " teams");
        }

        List<RatedPlayer> sortedPlayers = ratedPlayers.stream()
                .sorted(Comparator.comparingInt(RatedPlayer::rating).reversed())
                .toList();

        List<Attempt> attempts = new ArrayList<>();
        double bestSpread = Double.MAX_VALUE;
        for (int i = 0; i < ATTEMPTS; i++) {
            List<List<RatedPlayer>> teams = serpentine(shuffleWithTiers(sortedPlayers, teamCount), teamCount);
            double spread = spread(teams);
            bestSpread = Math.min(bestSpread, spread);
            attempts.add(new Attempt(teams, spread));
        }

        double limit = bestSpread + TOLERANCE;
        List<Attempt> acceptable = attempts.stream().filter(attempt -> attempt.spread() <= limit).toList();
        return acceptable.get(random.nextInt(acceptable.size())).teams;
    }

    /**
     * Distributes players into teams using a serpentine (snake) draft order.
     * <p>
     * Alternates draft direction across rounds (forward in even rounds, backward in odd rounds)
     * to ensure equitable distribution of player skill levels across teams.
     *
     * @param orderedPlayers the list of players ordered for drafting
     * @param teamCount      the number of teams to distribute players into
     * @return a list of teams populated with players
     */
    private List<List<RatedPlayer>> serpentine(List<RatedPlayer> orderedPlayers, int teamCount) {
        List<List<RatedPlayer>> teams = new ArrayList<>();
        for (int i = 0; i < teamCount; i++) {
            teams.add(new ArrayList<>());
        }

        for (int i = 0; i < orderedPlayers.size(); i++) {
            int round = i / teamCount;
            int pos = i % teamCount;
            int teamIndex = (round % 2 == 0) ? pos : teamCount - 1 - pos;
            teams.get(teamIndex).add(orderedPlayers.get(i));
        }

        return teams;
    }

    /**
     * Groups sorted players into tiers of the specified size and shuffles players within each tier.
     * <p>
     * This introduces randomness into team assignments while preserving comparable skill brackets
     * across drafts.
     *
     * @param sortedPlayers the list of players sorted by rating in descending order
     * @param tierSize      the number of players per tier, typically matching the team count
     * @return a new list of players with each tier shuffled internally
     */
    private List<RatedPlayer> shuffleWithTiers(List<RatedPlayer> sortedPlayers, int tierSize) {
        List<RatedPlayer> result = new ArrayList<>(sortedPlayers.size());
        for (int from = 0; from < sortedPlayers.size(); from += tierSize) {
            List<RatedPlayer> tier = new ArrayList<>(sortedPlayers.subList(from, Math.min(from + tierSize, sortedPlayers.size())));
            Collections.shuffle(tier);
            result.addAll(tier);
        }
        return result;
    }

    /**
     * Calculates the spread between the highest and lowest average player ratings among teams.
     *
     * @param teams the list of teams to evaluate
     * @return the difference between the maximum and minimum average team ratings
     */
    private double spread(List<List<RatedPlayer>> teams) {
        DoubleSummaryStatistics statistics = teams.stream()
                .mapToDouble(team -> team.stream().mapToInt(RatedPlayer::rating).average().orElse(0))
                .summaryStatistics();
        return statistics.getMax() - statistics.getMin();
    }

    /**
     * Represents a single team distribution attempt and its evaluated rating spread.
     *
     * @param teams  the generated list of teams
     * @param spread the difference between the highest and lowest average team ratings
     */
    private record Attempt(List<List<RatedPlayer>> teams, double spread) {
    }
}

