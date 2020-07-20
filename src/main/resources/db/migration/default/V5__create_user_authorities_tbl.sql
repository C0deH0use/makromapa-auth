CREATE TABLE user_authority
(
    id        UUID DEFAULT uuid_generate_v4() NOT NULL PRIMARY KEY,
    user_id   UUID                            NOT NULL,
    authority VARCHAR(250)                    NOT NULL,
    CONSTRAINT fk_authorities_users FOREIGN KEY (user_id) REFERENCES app_user (id)
);
CREATE UNIQUE INDEX idx_auth_username on user_authority (user_id, authority);