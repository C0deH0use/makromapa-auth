INSERT INTO app_user (id, external_id, provider, terms_and_conditions_id, name, surname, email, picture, type, enabled, created, last_updated) VALUES
 ('aa6641c1-e9f4-417f-adf4-f71accc470cb', '118364847911502210416', 'GOOGLE', 1000, 'Makromapa Test01', 'Test01', 'test.makro01@gmail.com',
  'https://lh3.googleusercontent.com/-ikY_Cm72czw/AAAAAAAAAAI/AAAAAAAAAAA/AMZuuclOYs-lQmxAWahK4NhqYU-W2pcy7g/s96-c/photo.jpg',
  'PREMIUM_USER', true, now(), now());

INSERT INTO app_user (id, provider, email, password, terms_and_conditions_id, type, enabled, created, last_updated) VALUES
('bb2cc695-4788-4470-8292-8e2d1870cd53', 'BASIC_AUTH', 'user_1@example.com', '{bcrypt}$2a$10$VEoDAoI9XPdTRUlnEhyppOJ16hQ2n.kGz0uVixkEWQSbrV8ESO4eC', null, 'FREE_USER', true, now(), now());

INSERT INTO user_authority (user_id, authority) VALUES
('aa6641c1-e9f4-417f-adf4-f71accc470cb', 'ROLE_PREMIUM_CONTENT'),
('bb2cc695-4788-4470-8292-8e2d1870cd53', 'ROLE_FREE_CONTENT');
