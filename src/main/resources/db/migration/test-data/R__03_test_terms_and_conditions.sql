INSERT INTO public.terms_and_conditions (id, contract_pl, contract_en, created, last_updated)
VALUES (nextval('terms_and_conditions_seq'), 'TESTOWY_REGULAMIN', 'TEST_TERMS_CONDITIONS', now(), now()),
       (nextval('terms_and_conditions_seq'), 'TESTOWY_REGULAMIN_2', 'TEST_TERMS_CONDITIONS_2', now() - interval '1 hour', now() - interval '1 hour');