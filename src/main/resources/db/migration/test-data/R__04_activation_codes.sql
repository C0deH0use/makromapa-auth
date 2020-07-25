INSERT INTO user_activation_code (id, draft_user_id, code, enabled, expires_on, created, last_updated)
VALUES ('ac921e1f-9b96-41e3-a977-a71f8144a788', '96718946-4ebd-4637-ae02-5cf2b5bc1bb2', '03c88cfd49', true, now() + INTERVAL '6 hour', now(), now()),
       ('8367b4a6-9e84-4fd3-a03c-388a02ad462b', '876003b2-2454-47b1-8145-0037e7069178', 'S0905WDgha', true, now() - INTERVAL '1 hour', now(), now()),
       ('2b6a195a-10df-4115-8701-8477d215788e', '0385d294-acf8-474b-b265-102f76ef9ae2', 'HHEH8x01B6', false, now() + INTERVAL '6 hour', now(), now());