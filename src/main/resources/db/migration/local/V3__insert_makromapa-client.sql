INSERT INTO oauth_client_details (client_id, resource_ids, client_secret, scope, authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, refresh_token_validity, additional_information, autoapprove)
VALUES ('makromapa-mobile', 'makromapa-mobile', '{bcrypt}$2a$10$6DLJKRbEExlUJSfGCwW1mOV1fpxyy2Vq/s6zRVqeiCYLOFp82pb4C', 'USER', 'external-token,refresh_token', null, 'ROLE_CLIENT', 58060800, 600, '{}', 'external-token'),
       ('basic-auth-makromapa-mobile', 'makromapa-mobile', '{bcrypt}$2a$10$6DLJKRbEExlUJSfGCwW1mOV1fpxyy2Vq/s6zRVqeiCYLOFp82pb4C', 'USER', 'password,external-token', null, 'ROLE_CLIENT,ROLE_REGISTER', 604800, 0, '{}', 'external-token'),
       ('makromapa-admin', 'makromapa-admin', '{bcrypt}$2a$10$6DLJKRbEExlUJSfGCwW1mOV1fpxyy2Vq/s6zRVqeiCYLOFp82pb4C', 'ADMIN', 'password', null, 'ROLE_ADMIN_USER', 21600, 600, '{}', null),
       ('userinfo-auth', 'userinfo-auth', '{bcrypt}$2a$10$6DLJKRbEExlUJSfGCwW1mOV1fpxyy2Vq/s6zRVqeiCYLOFp82pb4C', 'USER', 'password', null, 'ROLE_USER_INFO_AUTH', 0, 0, '{}', null),
       ('makromapa-backend', 'makromapa-backend', '{bcrypt}$2a$10$6DLJKRbEExlUJSfGCwW1mOV1fpxyy2Vq/s6zRVqeiCYLOFp82pb4C', 'ADMIN', 'password', null, 'ROLE_MAKROMAPA_BACKEND', 0, 0, '{}', null);