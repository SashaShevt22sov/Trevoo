CREATE TABLE chat_participant
(
    id        BIGSERIAL PRIMARY KEY,
    chat_id   BIGINT    NOT NULL,
    user_id   BIGINT    NOT NULL,
    joined_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_chat
        FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE
);