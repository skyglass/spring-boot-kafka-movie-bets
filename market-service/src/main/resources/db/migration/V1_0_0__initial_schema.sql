CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS market;

CREATE TABLE market (
      id uuid NOT NULL,
      item1 character varying COLLATE pg_catalog."default" NOT NULL,
      item2 character varying COLLATE pg_catalog."default" NOT NULL,
      status character varying COLLATE pg_catalog."default" NOT NULL,
      closes_at TIMESTAMP NOT NULL,
      open BOOLEAN NOT NULL DEFAULT TRUE,
      result int,
      CONSTRAINT market_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_market_status ON market (status);