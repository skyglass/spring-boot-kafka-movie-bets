CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS customer;

CREATE TABLE customer (
    id uuid NOT NULL,
    username character varying COLLATE pg_catalog."default" NOT NULL,
    full_name character varying COLLATE pg_catalog."default" NOT NULL,
    balance numeric(20,8) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT customer_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_customer_username ON customer (username);

DROP TABLE IF EXISTS wallet_request;

CREATE TABLE wallet_request(
    id uuid NOT NULL,
    request_id uuid NOT NULL,
    CONSTRAINT wallet_request_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_wallet_request ON wallet_request (request_id);