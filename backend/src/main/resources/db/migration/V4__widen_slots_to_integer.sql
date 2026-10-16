-- V4__widen_slots_to_integer.sql
-- Alinha o tipo da coluna slots com o Integer usado na entidade Match
-- (SMALLINT/int2 no Postgres nao bate com o mapeamento padrao de Integer no Hibernate).

ALTER TABLE matches ALTER COLUMN slots TYPE INTEGER;