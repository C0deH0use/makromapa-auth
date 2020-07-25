INSERT INTO app_user (id, external_id, provider, terms_and_conditions_id, name, surname, email, picture, type, enabled, created, last_updated) VALUES
 ('aa6641c1-e9f4-417f-adf4-f71accc470cb', '118364847911502210416', 'GOOGLE', 1000, 'Makromapa Test01', 'Test01', 'test.makro01@gmail.com',
  'https://lh3.googleusercontent.com/-ikY_Cm72czw/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclOYs-lQmxAWahK4NhqYU-W2pcy7g/s96-c/photo.jpg',
  'PREMIUM_USER', true, now(), now());

INSERT INTO app_user (id, provider, email, type, password, enabled, created, last_updated) VALUES
('bb2cc695-4788-4470-8292-8e2d1870cd53', 'BASIC_AUTH', 'user_1@example.com', 'FREE_USER', '{bcrypt}$2a$10$VEoDAoI9XPdTRUlnEhyppOJ16hQ2n.kGz0uVixkEWQSbrV8ESO4eC', true, now(), now()),
('96718946-4ebd-4637-ae02-5cf2b5bc1bb2', 'BASIC_AUTH', 'draft_user_1@email.pl', 'DRAFT_USER', '{bcrypt}$2a$10$fjdjLuESQNH1odeqM7MOeOqUFQO.e0r5CfbODkJ2kGHTGay3Fu1z6', false, now(), now()),
('876003b2-2454-47b1-8145-0037e7069178', 'BASIC_AUTH', 'draft_user_2@email.pl', 'DRAFT_USER', '{bcrypt}$2a$10$fjdjLuESQNH1odeqM7MOeOqUFQO.e0r5CfbODkJ2kGHTGay3Fu1z6', false, now(), now()),
('0385d294-acf8-474b-b265-102f76ef9ae2', 'BASIC_AUTH', 'draft_user_3@email.pl', 'DRAFT_USER', '{bcrypt}$2a$10$fjdjLuESQNH1odeqM7MOeOqUFQO.e0r5CfbODkJ2kGHTGay3Fu1z6', false, now(), now());

INSERT INTO user_authority (user_id, authority) VALUES
('aa6641c1-e9f4-417f-adf4-f71accc470cb', 'ROLE_PREMIUM_CONTENT'),
('bb2cc695-4788-4470-8292-8e2d1870cd53', 'ROLE_FREE_CONTENT');

