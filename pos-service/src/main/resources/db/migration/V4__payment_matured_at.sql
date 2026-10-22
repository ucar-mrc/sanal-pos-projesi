-- =====================================================
-- V4: Payment matured_at
-- Ödeme available_balance'a taşındığında doldurulur.
-- =====================================================

ALTER TABLE payments ADD COLUMN matured_at TIMESTAMPTZ;

CREATE INDEX idx_payments_matured_at ON payments(matured_at);