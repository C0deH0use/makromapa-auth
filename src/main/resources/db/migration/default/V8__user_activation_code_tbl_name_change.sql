ALTER TABLE user_activation_code
    RENAME COLUMN draft_user_id TO user_id;

ALTER TABLE user_activation_code
    ADD COLUMN code_type TEXT;

UPDATE user_activation_code
    SET code_type = 'REGISTRATION';

ALTER TABLE user_activation_code
    ALTER COLUMN code_type SET NOT NULL;

ALTER TABLE user_activation_code
    RENAME TO user_verification_code;


alter table user_verification_code drop constraint user_activation_code_draft_user_id_key;
--
-- alter table user_verification_code
--     add constraint user_verification_code_draft_user_id_key
--         unique (user_id, code_type);

