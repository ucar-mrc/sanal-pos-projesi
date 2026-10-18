-- =====================================================
-- V2: Seed commission rates
-- Vade → komisyon oranı eşlemesi
-- =====================================================

INSERT INTO commission_rates (settlement_days, rate, active) VALUES
                                                                 (0,  0.0345, TRUE),   -- T+0  → %3.45
                                                                 (7,  0.0250, TRUE),   -- T+7  → %2.50
                                                                 (15, 0.0180, TRUE),   -- T+15 → %1.80
                                                                 (30, 0.0100, TRUE),   -- T+30 → %1.00
                                                                 (45, 0.0000, TRUE);   -- T+45 → %0.00