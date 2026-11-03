-- V10__add_soft_delete_to_user.sql
-- Exclusão lógica de contas: o registro permanece (o histórico de partidas, times, votos e cartões depende dele),
-- mas os dados pessoais são anonimizados pela aplicação quando o usuário exclui a conta.

ALTER TABLE tb_user
    ADD COLUMN deleted_at TIMESTAMP;