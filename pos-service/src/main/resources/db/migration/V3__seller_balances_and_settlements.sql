-- =====================================================
-- V3: Seller balances and settlements
-- =====================================================

-- Satıcı cüzdanları
CREATE TABLE seller_balances (
                                 id                  BIGSERIAL PRIMARY KEY,
                                 seller_id           BIGINT        NOT NULL UNIQUE,   -- bank_db'deki müşteri ID
                                 available_balance   NUMERIC(19,4) NOT NULL DEFAULT 0,
                                 pending_balance     NUMERIC(19,4) NOT NULL DEFAULT 0,
                                 total_withdrawn     NUMERIC(19,4) NOT NULL DEFAULT 0,
                                 currency            VARCHAR(3)    NOT NULL,
                                 created_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                                 updated_at          TIMESTAMPTZ
);

CREATE INDEX idx_seller_balances_seller_id ON seller_balances(seller_id);

-- Satıcıya yapılan ödemeler (settlement geçmişi)
CREATE TABLE settlements (
                             id                    BIGSERIAL PRIMARY KEY,
                             seller_id             BIGINT        NOT NULL,
                             amount                NUMERIC(19,4) NOT NULL,      -- banka hesabına gönderilen tutar
                             currency              VARCHAR(3)    NOT NULL,
                             status                VARCHAR(20)   NOT NULL,      -- PENDING, COMPLETED, FAILED
                             bank_transaction_ref  VARCHAR(64),                 -- bank-service transfer referansı
                             requested_at          TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                             completed_at          TIMESTAMPTZ,
                             failure_reason        VARCHAR(500)
);

CREATE INDEX idx_settlements_seller_id ON settlements(seller_id);
CREATE INDEX idx_settlements_status ON settlements(status);
CREATE INDEX idx_settlements_requested_at ON settlements(requested_at);