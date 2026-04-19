CREATE TABLE chat
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255),
    is_group   BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);