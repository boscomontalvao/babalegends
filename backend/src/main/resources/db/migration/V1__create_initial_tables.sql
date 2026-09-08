-- V1__create_initial_tables.sql
-- Initial schema for BabaLegends

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    picture    VARCHAR(255),
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE groups
(
    id         UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    name       VARCHAR(150) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE group_members
(
    id         UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    group_id   UUID        NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    user_id    UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    role       VARCHAR(20) NOT NULL DEFAULT 'member',
    created_at TIMESTAMP   NOT NULL DEFAULT now(),
    UNIQUE (group_id, user_id)
);

CREATE TABLE player_attributes
(
    id          UUID PRIMARY KEY  DEFAULT gen_random_uuid(),
    user_id     UUID     NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    card_picture VARCHAR(255),
    overall     SMALLINT NOT NULL DEFAULT 50,
    pace        SMALLINT NOT NULL DEFAULT 50,
    shooting    SMALLINT NOT NULL DEFAULT 50,
    passing     SMALLINT NOT NULL DEFAULT 50,
    dribbling   SMALLINT NOT NULL DEFAULT 50,
    defending   SMALLINT NOT NULL DEFAULT 50,
    physicality SMALLINT NOT NULL DEFAULT 50
);

CREATE TABLE matches
(
    id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    group_id     UUID         NOT NULL REFERENCES groups (id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP    NOT NULL,
    location     VARCHAR(200) NOT NULL,
    slots        SMALLINT     NOT NULL,
    status       VARCHAR(20)  NOT NULL DEFAULT 'scheduled'
);

CREATE TABLE attendances
(
    id           UUID PRIMARY KEY     DEFAULT gen_random_uuid(),
    match_id     UUID        NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    user_id      UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    status       VARCHAR(20) NOT NULL DEFAULT 'pending',
    responded_at TIMESTAMP,
    UNIQUE (match_id, user_id)
);

CREATE TABLE teams
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID        NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    name     VARCHAR(50) NOT NULL
);

CREATE TABLE team_players
(
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    team_id UUID NOT NULL REFERENCES teams (id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (team_id, user_id)
);

CREATE TABLE votes
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    match_id UUID        NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    voter_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    voted_id UUID        NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    UNIQUE (match_id, voter_id, category)
);