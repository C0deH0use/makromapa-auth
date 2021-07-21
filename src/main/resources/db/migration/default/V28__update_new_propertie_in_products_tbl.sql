UPDATE product
SET premium_feature  = 'DISABLE_ADS',
    expires_in_weeks = 1
WHERE reason = 'USE';

UPDATE product
SET premium_feature = 'DISABLE_ADS',
    expires_in_weeks = 0
WHERE reason = 'PURCHASE'
  AND name = 'ads_removal';

UPDATE product
SET premium_feature = 'PREMIUM',
    expires_in_weeks = 4
WHERE reason = 'PURCHASE'
  AND name = 'sub_premium';
