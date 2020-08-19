CREATE SEQUENCE IF NOT EXISTS terms_and_conditions_seq
    INCREMENT BY 1
    MINVALUE 1000
    START WITH 1000;

CREATE TABLE terms_and_conditions
(
    id                      BIGINT DEFAULT nextval('terms_and_conditions_seq') NOT NULL PRIMARY KEY,
    contract_pl             TEXT                             NOT NULL,
    contract_en             TEXT                             NOT NULL,

    created                 TIMESTAMP                                NOT NULL,
    last_updated            TIMESTAMP                                NOT NULL
);
