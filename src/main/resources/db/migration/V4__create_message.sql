CREATE TABLE message
(
    id        BIGSERIAL PRIMARY KEY,
    chat_id   BIGINT    NOT NULL,
    sender_id BIGINT    NOT NULL,
    content   TEXT      NOT NULL,
    sent_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    is_read   BOOLEAN   NOT NULL DEFAULT FALSE,

    CONSTRAINT fk_message_chat
        FOREIGN KEY (chat_id) REFERENCES chat (id) ON DELETE CASCADE,

    CONSTRAINT fk_message_sender
        FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE
);