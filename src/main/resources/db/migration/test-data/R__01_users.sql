INSERT INTO app_user (id, external_id, provider, terms_and_conditions_id, name, surname, email, picture, type, enabled, points, created, last_updated)
VALUES ('aa6641c1-e9f4-417f-adf4-f71accc470cb', '118364847911502210416', 'GOOGLE', 1001, 'Makromapa Test01', 'Test01', 'test.makro01@gmail.com',
        'https://lh3.googleusercontent.com/-ikY_Cm72czw/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclOYs-lQmxAWahK4NhqYU-W2pcy7g/s96-c/photo.jpg',
        'FREE_USER', true, 2000, now(), now());

INSERT INTO app_user (id, provider, email, points, type, password, enabled, created, last_updated)
VALUES ('bb2cc695-4788-4470-8292-8e2d1870cd53', 'BASIC_AUTH', 'user_1@example.com', 500, 'FREE_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', true, now(), now()),
       ('228a4e7d-7023-49e4-afff-e7005c06225d', 'BASIC_AUTH', 'user_2@example.com', 0, 'FREE_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', true, now(), now()),
       ('401c7fa3-ff8f-4aa6-a43f-299e6a48b16c', 'BASIC_AUTH', 'admin@example.com', 0, 'ADMIN_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', true, now(), now()),
       ('96718946-4ebd-4637-ae02-5cf2b5bc1bb2', 'BASIC_AUTH', 'draft_user_1@email.pl', 0, 'DRAFT_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', false, now(), now()),
       ('876003b2-2454-47b1-8145-0037e7069178', 'BASIC_AUTH', 'draft_user_2@email.pl', 0, 'DRAFT_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', false, now(), now()),
       ('0385d294-acf8-474b-b265-102f76ef9ae2', 'BASIC_AUTH', 'draft_user_3@email.pl', 0, 'DRAFT_USER', '{bcrypt}$2a$10$udeqZubfL1eKSEGQnwAaiuopm6WS50m3QMBPMQ8g.NfI1wnQ6Cyhi', false, now(), now());

INSERT INTO user_authority (user_id, authority, expiry_date)
VALUES ('aa6641c1-e9f4-417f-adf4-f71accc470cb', 'ROLE_FREE_USER', null),
       ('aa6641c1-e9f4-417f-adf4-f71accc470cb', 'ROLE_DISABLE_ADS', now() + interval '10 hour'),
       ('aa6641c1-e9f4-417f-adf4-f71accc470cb', 'ROLE_PREMIUM', now() + interval '2 day'),
       ('bb2cc695-4788-4470-8292-8e2d1870cd53', 'ROLE_FREE_USER', null),
       ('401c7fa3-ff8f-4aa6-a43f-299e6a48b16c', 'ROLE_ADMIN_USER', null);
