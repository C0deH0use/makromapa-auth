INSERT INTO user_verification_code (id, user_id, code, code_type, enabled, expires_on, created, last_updated)
VALUES ('ac921e1f-9b96-41e3-a977-a71f8144a788', '96718946-4ebd-4637-ae02-5cf2b5bc1bb2', '03c88cfd49', 'REGISTRATION', true, now() + INTERVAL '6 hour', now(), now()),
       ('8367b4a6-9e84-4fd3-a03c-388a02ad462b', '876003b2-2454-47b1-8145-0037e7069178', 'S0905WDgha', 'REGISTRATION', true, now() - INTERVAL '1 hour', now(), now()),
       ('2b6a195a-10df-4115-8701-8477d215788e', '0385d294-acf8-474b-b265-102f76ef9ae2', 'HHEH8x01B6', 'REGISTRATION', false, now() + INTERVAL '6 hour', now(), now()),
       ('8a8aea25-467e-4fb9-99c2-ce30c442a373', 'bb2cc695-4788-4470-8292-8e2d1870cd53', '123456', 'RESET_PASSWORD', true, now() + INTERVAL '6 hour', now(), now())
;