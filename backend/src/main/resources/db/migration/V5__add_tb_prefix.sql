-- V5__add_tb_prefix.sql
-- Aplica o prefixo "tb_" a todas as tabelas.

ALTER TABLE users RENAME TO tb_user;
ALTER TABLE groups RENAME TO tb_group;
ALTER TABLE group_members RENAME TO tb_group_member;
ALTER TABLE matches RENAME TO tb_match;
ALTER TABLE player_attributes RENAME TO tb_player_attributes;
ALTER TABLE attendances RENAME TO tb_attendance;
ALTER TABLE teams RENAME TO tb_team;
ALTER TABLE team_players RENAME TO tb_team_player;
ALTER TABLE votes RENAME TO tb_vote;