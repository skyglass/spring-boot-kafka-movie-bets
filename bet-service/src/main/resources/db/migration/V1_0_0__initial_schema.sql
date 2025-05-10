CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TABLE IF EXISTS bet;

CREATE TABLE bet(
    id uuid NOT NULL,
    customer_id character varying COLLATE pg_catalog."default" NOT NULL,
    market_id uuid NOT NULL,
    market_name character varying COLLATE pg_catalog."default" NOT NULL,
    stake int NOT NULL,
    result int NOT NULL,
    status character varying COLLATE pg_catalog."default" NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    bet_won BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT bet_pkey PRIMARY KEY (id)
);

CREATE INDEX idx_bet_market_id ON bet (market_id);
CREATE INDEX idx_bet_customer_id ON bet (customer_id);
CREATE INDEX idx_bet_result ON bet (result);
CREATE INDEX idx_bet_status ON bet (status);

CREATE INDEX idx_bet_market_result ON bet (market_id, result);
CREATE INDEX idx_bet_market_status ON bet (market_id, status);
CREATE UNIQUE INDEX idx_bet_customer_market ON bet (customer_id, market_id);

DROP TABLE IF EXISTS market_settle_status;

CREATE TABLE market_settle_status(id uuid NOT NULL,
                               market_id uuid NOT NULL,
                               expected_count int NOT NULL,
                               finished_count int NOT NULL,
                               CONSTRAINT market_settle_status_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_market_settle_status_market_id ON market_settle_status (market_id);

DROP TABLE IF EXISTS bet_settle_request;

CREATE TABLE bet_settle_request(
                               id uuid NOT NULL,
                               request_id uuid NOT NULL,
                               market_id uuid NOT NULL,
                               CONSTRAINT bet_settle_request_pkey PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_bet_settle_request_request_id ON bet_settle_request (request_id);
CREATE INDEX idx_bet_settle_request_market_id ON bet_settle_request (market_id);