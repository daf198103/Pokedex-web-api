-- id is the PokeAPI/Pokedex id (assigned, not generated)
CREATE TABLE pokemon (
    id             INTEGER       PRIMARY KEY,
    name           VARCHAR(100)  NOT NULL,
    sprite_url     VARCHAR(500),
    category       VARCHAR(100),
    weight         INTEGER       NOT NULL DEFAULT 0,
    height         INTEGER       NOT NULL DEFAULT 0,
    abilities      VARCHAR(1000) NOT NULL DEFAULT '',
    -- proprietary fields (never overwritten by a re-sync)
    localized_name VARCHAR(100),
    region         VARCHAR(100),
    tags           VARCHAR(1000) NOT NULL DEFAULT '',
    version        BIGINT        NOT NULL DEFAULT 0,
    created_at     TIMESTAMP     NOT NULL,
    updated_at     TIMESTAMP     NOT NULL
);
CREATE INDEX idx_pokemon_name ON pokemon (name);
