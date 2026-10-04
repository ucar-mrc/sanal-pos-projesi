-- =====================================================
-- V2: Transactions and Idempotency
-- =====================================================

CREATE TABLE transactions (
                              id                BIGSERIAL PRIMARY KEY,
                              account_id        BIGINT        NOT NULL REFERENCES accounts(id),
                              type              VARCHAR(20)   NOT NULL,       -- DEBIT, CREDIT, TRANSFER_IN, TRANSFER_OUT
                              amount            NUMERIC(19,4) NOT NULL,
                              currency          VARCHAR(3)    NOT NULL,
                              balance_after     NUMERIC(19,4) NOT NULL,       -- işlem sonrası hesap bakiyesi
                              reference_id      VARCHAR(64),                  -- transferler için ortak referans
                              description       VARCHAR(500),
                              created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_reference_id ON transactions(reference_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

CREATE TABLE idempotency_keys (
                                  id                BIGSERIAL PRIMARY KEY,
                                  key               VARCHAR(255)  NOT NULL UNIQUE,
                                  endpoint          VARCHAR(255)  NOT NULL,
                                  response_body     TEXT,                          -- ilk cevabı saklayıp tekrar dönmek için
                                  response_status   INT,                           -- ilk HTTP status
                                  created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_idempotency_keys_key ON idempotency_keys(key);