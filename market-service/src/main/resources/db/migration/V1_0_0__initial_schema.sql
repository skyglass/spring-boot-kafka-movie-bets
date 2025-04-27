CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS market;

CREATE TABLE market (
      id uuid NOT NULL,
      item_1 character varying COLLATE pg_catalog."default" NOT NULL,
      item_2 character varying COLLATE pg_catalog."default" NOT NULL,
      status character varying COLLATE pg_catalog."default" NOT NULL,
      closes_at TIMESTAMP NOT NULL,
      result int,
      CONSTRAINT market_pkey PRIMARY KEY (id)
);