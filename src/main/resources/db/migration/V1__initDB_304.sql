/*
CREATE SCHEMA IF NOT EXISTS service304;
CREATE TABLE IF NOT EXISTS service304.stock
(
    id              VARCHAR(255) not null,
    title           VARCHAR(255),
    description     VARCHAR(255),
    price           INTEGER(50),
    amount          VARCHAR(255),
    reserved_count  VARCHAR(255),
    category        VARCHAR(255),
    primary key (id)
    );

CREATE TABLE IF NOT EXISTS service304.catalog_item
(
    id              VARCHAR(255) not null,
    product_id      VARCHAR(255) REFERENCES service304.stock (id),
    amount          INTEGER(50),
    primary key (id)
    );

CREATE TABLE IF NOT EXISTS service304.shopping_cart
(
    id       VARCHAR(255) not null,
    status   INTEGER(10),
    primary key (id)
    );

CREATE TABLE IF NOT EXISTS service304.user
(
    id              VARCHAR(255) not null,
    ip_address      INTEGER(10),
    name            VARCHAR(255),
    email           VARCHAR(255),
    phone           INTEGER(50),
    last_basket_id  VARCHAR(255) REFERENCES service304.shopping_cart (id),
    primary key (id)
    );

CREATE TABLE IF NOT EXISTS service304.order
(
    id          VARCHAR(255) not null,
    status      INTEGER(10),
    basket_id   VARCHAR(255) REFERENCES service304.shopping_cart (id),
    user_id     VARCHAR(255) REFERENCES service304.user (id),
    date        DATE,
    primary key (id)
    );

CREATE TABLE IF NOT EXISTS service304.basket_catalog_item
(
    basket_id       VARCHAR(255) REFERENCES service304.shopping_cart (id),
    catalog_item_id VARCHAR(255) REFERENCES service304.catalog_item (id)
    );

CREATE TABLE IF NOT EXISTS service304.payment
  (
    order_id        VARCHAR(255) REFERENCES service304.order (id),
    type            INTEGER(10),
    amount          INTEGER(10),
    time            TIME
);

CREATE TABLE IF NOT EXISTS service304.delivery
  (
    id              VARCHAR(255) not null,
    order_id        VARCHAR(255) REFERENCES service304.order (id),
    address         VARCHAR(255),
    slot            INTEGER(255),
    primary key (id)
);*/
