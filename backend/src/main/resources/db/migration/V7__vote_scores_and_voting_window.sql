-- V8__vote_scores_and_voting_window.sql
-- Votação passa a ser uma nota (0 a 10) por jogador avaliado, em vez de um voto por categoria.
-- A coluna category continua existindo e hoje só recebe 'OVERALL', mas pode receber atributos individuais também.
-- tb_match.completed_at marca o fim da partida e define o prazo de votação.
-- Marca quando as notas de uma partida já foram aplicadas nas cartas.
-- overall = média simples dos seis atributos. Corrige os dados que já existem.

ALTER TABLE tb_match ADD COLUMN completed_at TIMESTAMP;

ALTER TABLE tb_vote ADD COLUMN score SMALLINT NOT NULL;

ALTER TABLE tb_vote ADD CONSTRAINT ck_vote_score CHECK (score BETWEEN 0 AND 10);
ALTER TABLE tb_vote ADD CONSTRAINT ck_vote_not_self CHECK (voter_id <> voted_id);

ALTER TABLE tb_vote DROP CONSTRAINT votes_match_id_voter_id_category_key;

ALTER TABLE tb_vote
    ADD CONSTRAINT uq_vote_match_voter_voted_category UNIQUE (match_id, voter_id, voted_id, category);

ALTER TABLE tb_match ADD COLUMN votes_applied_at TIMESTAMP;

UPDATE tb_player_attributes
SET overall = ROUND((pace + shooting + passing + dribbling + defending + physicality) / 6.0);