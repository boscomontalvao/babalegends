-- V3__rename_primary_keys.sql
-- Renomeia a coluna "id" de cada tabela para um nome especifico
-- (ex: users.id -> users.user_id), evitando ambiguidade em joins
-- e deixando o schema mais legivel.

ALTER TABLE users RENAME COLUMN id TO user_id;
ALTER TABLE groups RENAME COLUMN id TO group_id;
ALTER TABLE group_members RENAME COLUMN id TO group_member_id;
ALTER TABLE player_attributes RENAME COLUMN id TO player_attribute_id;
ALTER TABLE matches RENAME COLUMN id TO match_id;
ALTER TABLE attendances RENAME COLUMN id TO attendance_id;
ALTER TABLE teams RENAME COLUMN id TO team_id;
ALTER TABLE team_players RENAME COLUMN id TO team_player_id;
ALTER TABLE votes RENAME COLUMN id TO vote_id;