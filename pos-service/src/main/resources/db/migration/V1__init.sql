-- =====================================================
-- V1: POS Service initial schema
-- =====================================================

-- Komisyon oran tablosu (vade → oran eşlemesi)
CREATE TABLE commission_rates (
                                  id                BIGSERIAL PRIMARY KEY,
                                  settlement_days   INT           NOT NULL UNIQUE,   -- 0, 7, 15, 30, 45
                                  rate              NUMERIC(5,4)  NOT NULL,           -- 0.0345 = %3.45
                                  active            BOOLEAN       NOT NULL DEFAULT TRUE,
                                  created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                                  updated_at        TIMESTAMPTZ
);

-- Ödeme işlemleri
CREATE TABLE payments (
                          id                    BIGSERIAL PRIMARY KEY,
                          order_id              BIGINT        NOT NULL,       -- ecommerce_db'deki order ID (cross-service ref)
                          customer_id           BIGINT        NOT NULL,       -- bank_db'deki müşteri ID
                          seller_id             BIGINT        NOT NULL,       -- bank_db'deki satıcı ID
                          amount                NUMERIC(19,4) NOT NULL,       -- toplam tutar
                          currency              VARCHAR(3)    NOT NULL,
                          status                VARCHAR(20)   NOT NULL,       -- PENDING, AUTH_OK, FAILED, REFUNDED
                          settlement_days       INT           NOT NULL,       -- satıcının seçtiği vade (T+0, T+7, ...)
                          commission_rate       NUMERIC(5,4)  NOT NULL,       -- o an uygulanan oran (snapshot)
                          commission_amount     NUMERIC(19,4) NOT NULL,       -- amount * commission_rate
                          net_amount            NUMERIC(19,4) NOT NULL,       -- amount - commission_amount
                          card_last4            VARCHAR(4)    NOT NULL,       -- maskeli gösterim için
                          idempotency_key       VARCHAR(64)   NOT NULL UNIQUE, -- tekrar koruması
                          bank_transaction_ref  VARCHAR(64),                  -- bank-service'teki transaction ID
                          description           VARCHAR(500),
                          created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          updated_at            TIMESTAMPTZ
);

CREATE INDEX idx_payments_order_id ON payments(order_id);
CREATE INDEX idx_payments_customer_id ON payments(customer_id);
CREATE INDEX idx_payments_seller_id ON payments(seller_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_created_at ON payments(created_at);