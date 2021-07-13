TRUNCATE product;

INSERT INTO product(reasons, points, name, description, enabled, created, last_updated)
VALUES ('EARN', 100, 'APPROVED_DISH_PROPOSAL', 'Earn points by adding or updating dishes that will be approved by MakroMapa administrators', true, now(), now()),
       ('PURCHASE', 0, 'sub_premium', 'Purchase product for MakroMapa PREMIUM content for one month', false, now(), now()),
       ('PURCHASE', 0, 'ads_removal', 'Purchase product to remove ads for lifetime', true, now(), now()),
       ('USE', 400, 'DISABLE_ADS', 'Disable Ads in MakroMapa for one week', true, now(), now());


