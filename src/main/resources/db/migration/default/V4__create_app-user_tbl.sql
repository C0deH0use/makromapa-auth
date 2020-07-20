CREATE TABLE app_user
(
    id                      UUID    DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    external_id             VARCHAR(255) UNIQUE,

    provider                VARCHAR(50)                        NOT NULL,
    terms_and_conditions_id BIGINT,

    name                    VARCHAR(255),
    surname                 VARCHAR(255),
    email                   VARCHAR(255) UNIQUE,
    picture                 VARCHAR(255),
    type                    VARCHAR(255),

    password                VARCHAR(250),
    enabled                 BOOLEAN DEFAULT FALSE NOT NULL,

    created                 TIMESTAMP                          NOT NULL,
    last_updated            TIMESTAMP                          NOT NULL
);

ALTER TABLE app_user
    ADD CONSTRAINT user_required_check
        CHECK ( (provider <> 'BASIC_AUTH' AND external_id IS NOT NULL) OR
                (provider = 'BASIC_AUTH' AND email IS NOT NULL AND password IS NOT NULL)
            );

CREATE UNIQUE INDEX idx_user_provider_external_id on app_user (provider, external_id);
CREATE UNIQUE INDEX idx_user_email on app_user (email);
