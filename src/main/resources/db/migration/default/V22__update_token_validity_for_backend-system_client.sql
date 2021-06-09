UPDATE oauth_client_details
SET access_token_validity = 21600, refresh_token_validity = 604800
WHERE client_id in ('makromapa-backend', 'makromapa-admin');