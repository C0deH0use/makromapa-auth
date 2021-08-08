UPDATE product
SET points = 1000
WHERE reason = 'USE'
  AND expires_in_weeks = 1
  AND premium_feature = 'DISABLE_ADS';
