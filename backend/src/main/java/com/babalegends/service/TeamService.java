package com.babalegends.service;

import com.babalegends.domain.RatedPlayer;
import com.babalegends.domain.TeamBalancer;
import com.babalegends.dto.CardResponseDto;
import com.babalegends.dto.MyTeamResponseDto;
import com.babalegends.dto.TeamPlayerResponseDto;
import com.babalegends.dto.TeamResponseDto;
import com.babalegends.dto.UserOverallDto;
import com.babalegends.entity.Match;
import com.babalegends.entity.PlayerAttributes;
import com.babalegends.entity.Team;
import com.babalegends.entity.TeamPlayer;
import com.babalegends.entity.User;
import com.babalegends.entity.enums.MatchStatus;
import com.babalegends.exception.ConflictException;
import com.babalegends.exception.NotFoundException;
import com.babalegends.mapper.CardMapper;
import com.babalegends.repository.TeamPlayerRepository;
import com.babalegends.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final int MIN_PLAYERS_PER_TEAM = 5;
    private static final int DEFAULT_RATING = 50;

    private final MatchService matchService;
    private final TeamPlayerRepository teamPlayerRepository;
    private final PlayerAttributesService playerAttributesService;
    private final GroupService groupService;
    private final AttendanceService attendanceService;
    private final TeamRepository teamRepository;
    private final TeamBalancer teamBalancer;
    private final CardMapper cardMapper;

    @Transactional
    public List<TeamResponseDto> drawTeams(UUID requesterId, UUID matchId, int teamCount) {
        Match match = matchService.requireMatchForUpdate(requesterId, matchId);
        groupService.requireAdmin(match.getGroup().getId(), requesterId);

        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new ConflictException("Teams can only be drawn for scheduled matches.");
        }

        if (match.getScheduledAt().isBefore(LocalDateTime.now())) {
            throw new ConflictException("Cannot draw teams for a past match.");
        }

        List<User> confirmedUsers = attendanceService.listConfirmedUsers(matchId);
        int required = teamCount * MIN_PLAYERS_PER_TEAM;

        if (confirmedUsers.size() < required) {
            throw new ConflictException("At least " + required + " confirmed players are needed for "
                    + teamCount + " teams (" + confirmedUsers.size() + " confirmed)");
        }

        List<UUID> confirmedUsersIds = confirmedUsers.stream().map(User::getId).toList();
        Map<UUID, UserOverallDto> playersOverall = playerAttributesService.getOverallDtoByUserIds(confirmedUsersIds);
        List<RatedPlayer> ratedPlayers = confirmedUsers.stream()
                .map(user -> new RatedPlayer(user, ratingOfDtoOrDefault(playersOverall, user.getId())))
                .toList();

        List<List<RatedPlayer>> drawTeams = teamBalancer.draw(ratedPlayers, teamCount);

        teamPlayerRepository.deleteByMatchId(matchId);
        teamRepository.deleteByMatchId(matchId);

        List<TeamResponseDto> result = new ArrayList<>();
        for (int i = 0; i < drawTeams.size(); i++) {
            List<RatedPlayer> members = drawTeams.get(i);

            Team team = new Team();
            team.setMatch(match);
            team.setName(teamName(i));
            teamRepository.save(team);

            teamPlayerRepository.saveAll(members.stream().map(player -> {
                TeamPlayer teamPlayer = new TeamPlayer();
                teamPlayer.setTeam(team);
                teamPlayer.setUser(player.user());
                return teamPlayer;
            }).toList());

            result.add(toDto(team, members, playersOverall));
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDto> getTeams(UUID requesterId, UUID matchId) {
        matchService.requireMatchAccess(requesterId, matchId);

        List<TeamPlayer> teamPlayers = teamPlayerRepository.findAllByMatchIdWithUserAndTeam(matchId);

        if (teamPlayers.isEmpty()) {
            return List.of();
        }

        Map<UUID, UserOverallDto> overall = playerAttributesService.getOverallDtoByUserIds(
                teamPlayers.stream()
                        .map(teamPlayer -> teamPlayer.getUser().getId())
                        .collect(Collectors.toSet())
        );

        Map<UUID, List<TeamPlayer>> byTeam = teamPlayers.stream()
                .collect(Collectors.groupingBy(teamPlayer ->
                        teamPlayer.getTeam().getId(), LinkedHashMap::new, Collectors.toList())
                );

        return byTeam.values().stream().map(list -> {
            Team team = list.getFirst().getTeam();
            List<RatedPlayer> members = list.stream()
                    .map(teamPlayer -> new RatedPlayer(teamPlayer.getUser(),
                            ratingOfDtoOrDefault(overall, teamPlayer.getUser().getId())))
                    .toList();
            return toDto(team, members, overall);
        }).toList();
    }

    @Transactional(readOnly = true)
    public MyTeamResponseDto getMyTeam(UUID requesterId, UUID matchId) {
        matchService.requireMatchAccess(requesterId, matchId);

        List<TeamPlayer> myTeamPlayers = teamPlayerRepository.findMyTeamPlayersByMatchIdAndUserIdWithUserAndTeam(
                matchId, requesterId);

        if (myTeamPlayers.isEmpty()) {
            throw new NotFoundException("You are not in a team for this match.");
        }

        List<UUID> myTeamPlayersIds = myTeamPlayers.stream()
                .map(teamPlayer -> teamPlayer.getUser().getId())
                .toList();

        Map<UUID, PlayerAttributes> cardsByUserId = playerAttributesService.getAttributesByUserIds(myTeamPlayersIds);
        List<CardResponseDto> players = myTeamPlayers.stream()
                .map(teamPlayer -> cardMapper.convertAttributesToCardResponse(teamPlayer.getUser(), cardsByUserId.get(teamPlayer.getUser().getId())))
                .sorted(Comparator.comparingInt(CardResponseDto::overall).reversed())
                .toList();

        int averageRating = (int) Math.round(players.stream()
                .mapToInt(CardResponseDto::overall)
                .average()
                .orElse(0));

        return new MyTeamResponseDto(myTeamPlayers.getFirst().getTeam().getName(), averageRating, players);
    }

    private String teamName(int index) {
        return String.valueOf((char) ('A' + index));
    }

    private int ratingOfDtoOrDefault(Map<UUID, UserOverallDto> overallByUserId, UUID userId) {
        UserOverallDto dto = overallByUserId.get(userId);
        return dto == null ? DEFAULT_RATING : dto.overall();
    }

    private TeamResponseDto toDto(Team team, List<RatedPlayer> members, Map<UUID, UserOverallDto> overallByUserId) {
        List<TeamPlayerResponseDto> players = members.stream()
                .sorted(Comparator.comparingInt(RatedPlayer::rating).reversed())
                .map(member -> {
                    User user = member.user();
                    UserOverallDto userOverallDto = overallByUserId.get(user.getId());
                    String cardPictureKey = userOverallDto == null ? null : userOverallDto.cardPicture();
                    return new TeamPlayerResponseDto(
                            user.getId(),
                            user.getName(),
                            cardMapper.getPictureUrl(cardPictureKey, user.getPicture()),
                            member.rating());
                })
                .toList();
        
        int averageRating = (int) Math.round(members.stream().mapToInt(RatedPlayer::rating)
                .average().orElse(0));
        return new TeamResponseDto(team.getId(), team.getName(), averageRating, players);
    }
}
