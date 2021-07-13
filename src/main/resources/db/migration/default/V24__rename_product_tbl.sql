ALTER SEQUENCE points_product_seq
    RENAME TO product_seq;

ALTER TABLE points_product
    RENAME TO product;

ALTER TABLE product
    ADD COLUMN enabled bool NOT NULL DEFAULT false;


ALTER SEQUENCE product_seq RESTART;