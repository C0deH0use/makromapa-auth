ALTER TABLE app_user
    ADD COLUMN nickname TEXT,
    ADD COLUMN show_nick_only BOOLEAN DEFAULT FALSE;