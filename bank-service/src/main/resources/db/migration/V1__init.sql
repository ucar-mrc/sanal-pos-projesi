-- =====================================================
-- V1: Bank Service initial schema
-- =====================================================

CREATE TABLE customers (
                           id           BIGSERIAL PRIMARY KEY,
                           tckn         VARCHAR(11)  NOT NULL UNIQUE,
                           first_name   VARCHAR(100) NOT NULL,
                           last_name    VARCHAR(100) NOT NULL,
                           email        VARCHAR(255) NOT NULL UNIQUE,
                           phone        VARCHAR(20),
                           created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                           updated_at   TIMESTAMPTZ
);

CREATE TABLE accounts (
                          id           BIGSERIAL PRIMARY KEY,
                          customer_id  BIGINT       NOT NULL REFERENCES customers(id),
                          iban         VARCHAR(34)  NOT NULL UNIQUE,
                          balance      NUMERIC(19,4) NOT NULL DEFAULT 0,
                          currency     VARCHAR(3)   NOT NULL,
                          account_type VARCHAR(20)  NOT NULL,
                          created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);

CREATE TABLE cards (
                       id               BIGSERIAL PRIMARY KEY,
                       customer_id      BIGINT       NOT NULL REFERENCES customers(id),
                       card_number      VARCHAR(19)  NOT NULL UNIQUE,
                       card_holder_name VARCHAR(200) NOT NULL,
                       expiry_month     INT          NOT NULL,
                       expiry_year      INT          NOT NULL,
                       cvv_hash         VARCHAR(255) NOT NULL,
                       card_type        VARCHAR(20)  NOT NULL,
                       active           BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                       updated_at       TIMESTAMPTZ
);

CREATE INDEX idx_cards_customer_id ON cards(customer_id);