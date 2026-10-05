-- =====================================================
-- V3: Card security - hash card number, remove CVV storage
-- =====================================================

-- Eski kolonları kaldır
ALTER TABLE cards DROP COLUMN card_number;
ALTER TABLE cards DROP COLUMN cvv_hash;

-- Yeni kolonlar ekle
ALTER TABLE cards ADD COLUMN card_number_hash VARCHAR(64) NOT NULL UNIQUE;
ALTER TABLE cards ADD COLUMN card_number_last4 VARCHAR(4) NOT NULL;