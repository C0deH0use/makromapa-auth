ALTER TABLE product
    ADD premium_feature TEXT NOT NULL DEFAULT 'NON';

ALTER TABLE product
    ADD expires_in_weeks int;