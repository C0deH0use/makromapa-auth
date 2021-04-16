UPDATE oauth_client_details
SET authorized_grant_types = 'password,client_credentials'
WHERE client_id = 'makromapa-backend';