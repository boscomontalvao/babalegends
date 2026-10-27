package com.babalegends.domain;

import com.babalegends.entity.User;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TeamBalancerTest {

    private static final int[] SEED_RATINGS = {80, 78, 75, 72, 68, 65, 62, 60, 55, 50};

    private static List<RatedPlayer> players(int... ratings) {
        return Arrays.stream(ratings).mapToObj(rating -> {
            User user = new User();
            user.setId(UUID.randomUUID());
            return new RatedPlayer(user, rating);
        }).toList();
    }

    private static double average(List<RatedPlayer> team) {
        return team.stream().mapToInt(RatedPlayer::rating).average().orElseThrow();
    }

    @Test
    void everyPlayerAppearsExactlyOnce() {
        TeamBalancer balancer = new TeamBalancer(new Random(1));
        Random data = new Random(7);
        for (int teamCount = 2; teamCount <= 4; teamCount++) {
            for (int n = teamCount * 5; n <= teamCount * 5 + 7; n++) {
                List<RatedPlayer> input = players(data.ints(n, 30, 95).toArray());

                List<List<RatedPlayer>> teams = balancer.draw(input, teamCount);

                List<RatedPlayer> all = teams.stream().flatMap(List::stream).toList();
                assertThat(teams).hasSize(teamCount);
                assertThat(all).containsExactlyInAnyOrderElementsOf(input);
            }
        }
    }

    @Test
    void teamSizesDifferByAtMostOne() {
        TeamBalancer balancer = new TeamBalancer(new Random(2));
        for (int teamCount = 2; teamCount <= 4; teamCount++) {
            for (int n = teamCount * 5; n <= teamCount * 5 + 7; n++) {
                List<List<RatedPlayer>> teams =
                        balancer.draw(players(new Random(n).ints(n, 30, 95).toArray()), teamCount);

                IntSummaryStatistics sizes = teams.stream().mapToInt(List::size).summaryStatistics();
                assertThat(sizes.getMax() - sizes.getMin()).isLessThanOrEqualTo(1);
            }
        }
    }

    @Test
    void seedPlayersWithTwoTeamsAreBalanced() {
        TeamBalancer balancer = new TeamBalancer(new Random(3));
        for (int i = 0; i < 50; i++) {
            List<List<RatedPlayer>> teams = balancer.draw(players(SEED_RATINGS), 2);

            assertThat(Math.abs(average(teams.get(0)) - average(teams.get(1)))).isLessThanOrEqualTo(2.0);
        }
    }

    @Test
    void redrawProducesDifferentTeams() {
        TeamBalancer balancer = new TeamBalancer(new Random(4));
        List<RatedPlayer> input = players(SEED_RATINGS);

        Set<Set<RatedPlayer>> firstTeams = new HashSet<>();
        for (int i = 0; i < 30; i++) {
            firstTeams.add(new HashSet<>(balancer.draw(input, 2).get(0)));
        }

        assertThat(firstTeams.size()).isGreaterThan(1);
    }

    @Test
    void allEqualRatingsDoNotBreak() {
        TeamBalancer balancer = new TeamBalancer(new Random(5));
        List<List<RatedPlayer>> teams = balancer.draw(players(60, 60, 60, 60, 60, 60, 60, 60, 60, 60), 2);

        assertThat(teams).allSatisfy(team -> assertThat(team).hasSize(5));
    }

    @Test
    void failsWhenThereAreFewerPlayersThanTeams() {
        TeamBalancer balancer = new TeamBalancer();

        assertThatThrownBy(() -> balancer.draw(players(70, 60), 3))
                .isInstanceOf(IllegalArgumentException.class);
    }
}