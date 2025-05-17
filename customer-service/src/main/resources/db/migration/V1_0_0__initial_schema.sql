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

DROP TABLE IF EXISTS fund_request;

CREATE TABLE fund_request(
    id uuid NOT NULL,
    request_id uuid NOT NULL,
    CONSTRAINT fund_request_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_fund_request ON fund_request (request_id);