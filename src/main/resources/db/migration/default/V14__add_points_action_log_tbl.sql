CREATE TABLE points_action_log
(
    id           UUID DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    user_id      UUID                            NOT NULL,
    reason       TEXT                            NOT NULL,
    points       INT                             NOT NULL,
    product_id   BIGINT                          NOT NULL,

    created      TIMESTAMP                       NOT NULL,
    last_updated TIMESTAMP                       NOT NULL
);