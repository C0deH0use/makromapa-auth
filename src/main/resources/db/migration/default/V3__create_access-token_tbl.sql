CREATE SEQUENCE IF NOT EXISTS access_token_seq
    INCREMENT BY 1
    MINVALUE 1000
    START WITH 1000;

CREATE TABLE access_token
(
    id                          BIGINT DEFAULT nextval('access_token_seq')  NOT NULL PRIMARY KEY,
    user_id                     BIGINT                                      NOT NULL,
    enabled                     BOOLEAN                                     NOT NULL,
    code                        TEXT                                        NOT NULL,
    refresh_code                TEXT                                        NOT NULL,
    expiry_date                 TIMESTAMP                                   NOT NULL,
    refresh_code_expiry_date    TIMESTAMP                                   NOT NULL,
    created                     TIMESTAMP                                   NOT NULL,
    last_updated                TIMESTAMP                                   NOT NULL
);
