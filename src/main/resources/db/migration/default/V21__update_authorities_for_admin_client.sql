UPDATE oauth_client_details
SET authorities = 'ROLE_ADMIN_USER'
WHERE client_id = 'makromapa-admin';