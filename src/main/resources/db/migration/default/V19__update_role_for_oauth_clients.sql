UPDATE oauth_client_details
SET scope = 'USER'
WHERE client_id = 'makromapa-backend';

UPDATE oauth_client_details
SET scope = 'USER,ADMIN'
WHERE client_id = 'makromapa-admin';