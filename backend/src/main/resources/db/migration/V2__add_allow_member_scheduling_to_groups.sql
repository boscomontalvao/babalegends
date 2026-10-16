-- V2__add_allow_member_scheduling_to_groups.sql
-- Permite que o grupo escolha se qualquer membro pode criar partidas,
-- ou se essa ação fica restrita apenas ao(s) admin(s) do grupo.

ALTER TABLE groups
    ADD COLUMN allow_member_scheduling BOOLEAN NOT NULL DEFAULT false;