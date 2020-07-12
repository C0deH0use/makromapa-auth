CREATE SEQUENCE IF NOT EXISTS macro_user_seq
    INCREMENT BY 1
    MINVALUE 1000
    START WITH 1000;

CREATE TABLE macro_user
(
    id                      BIGINT DEFAULT nextval('macro_user_seq') NOT NULL PRIMARY KEY,
    external_id             VARCHAR(255)                             NOT NULL UNIQUE,

    provider                VARCHAR(50)                              NOT NULL,
    terms_and_conditions_id BIGINT,

    name                    VARCHAR(255),
    surname                 VARCHAR(255),
    email                   VARCHAR(255),
    picture                 VARCHAR(255),
    type                    VARCHAR(255),

    created                 TIMESTAMP                                NOT NULL,
    last_updated            TIMESTAMP                                NOT NULL
);
