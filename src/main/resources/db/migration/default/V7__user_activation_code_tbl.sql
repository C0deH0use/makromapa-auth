CREATE TABLE user_activation_code
(
    id            UUID    DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    draft_user_id UUID UNIQUE,

    code          VARCHAR(255)                       NOT NULL,
    enabled       BOOLEAN DEFAULT FALSE,
    expires_on    TIMESTAMP                          NOT NULL,

    created       TIMESTAMP                          NOT NULL,
    last_updated  TIMESTAMP                          NOT NULL
);

CREATE UNIQUE INDEX idx_user_activation_code on user_activation_code (code);