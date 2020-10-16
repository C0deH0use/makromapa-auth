UPDATE oauth_client_details
    SET access_token_validity = 604800
    WHERE client_id = 'makromapa-mobile';