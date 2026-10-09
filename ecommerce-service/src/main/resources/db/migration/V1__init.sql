-- =====================================================
-- V1: E-commerce Service initial schema
-- =====================================================

-- Satıcı ürünleri
CREATE TABLE products (
                          id           BIGSERIAL PRIMARY KEY,
                          seller_id    BIGINT        NOT NULL,       -- bank_db'deki müşteri ID'si (cross-service reference)
                          name         VARCHAR(200)  NOT NULL,
                          description  VARCHAR(2000),
                          price        NUMERIC(19,4) NOT NULL,
                          currency     VARCHAR(3)    NOT NULL,
                          stock        INT           NOT NULL DEFAULT 0,
                          active       BOOLEAN       NOT NULL DEFAULT TRUE,
                          created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                          updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_products_seller_id ON products(seller_id);
CREATE INDEX idx_products_active ON products(active);

-- Müşteri sepetleri
CREATE TABLE carts (
                       id           BIGSERIAL PRIMARY KEY,
                       customer_id  BIGINT      NOT NULL UNIQUE,   -- bir müşterinin aktif tek sepeti
                       created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_carts_customer_id ON carts(customer_id);

-- Sepet kalemleri
CREATE TABLE cart_items (
                            id           BIGSERIAL PRIMARY KEY,
                            cart_id      BIGINT        NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                            product_id   BIGINT        NOT NULL REFERENCES products(id),
                            quantity     INT           NOT NULL,
                            unit_price   NUMERIC(19,4) NOT NULL,       -- sepete eklerken fiyat snapshot
                            created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items(product_id);

-- Siparişler
CREATE TABLE orders (
                        id           BIGSERIAL PRIMARY KEY,
                        customer_id  BIGINT        NOT NULL,
                        total_amount NUMERIC(19,4) NOT NULL,
                        currency     VARCHAR(3)    NOT NULL,
                        status       VARCHAR(20)   NOT NULL,       -- PENDING, PAID, FAILED, CANCELLED, REFUNDED
                        created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
                        updated_at   TIMESTAMPTZ
);

CREATE INDEX idx_orders_customer_id ON orders(customer_id);
CREATE INDEX idx_orders_status ON orders(status);

-- Sipariş kalemleri (snapshot)
CREATE TABLE order_items (
                             id           BIGSERIAL PRIMARY KEY,
                             order_id     BIGINT        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id   BIGINT        NOT NULL,       -- FK YOK, snapshot referansı
                             product_name VARCHAR(200)  NOT NULL,       -- o anki isim
                             quantity     INT           NOT NULL,
                             unit_price   NUMERIC(19,4) NOT NULL,       -- o anki fiyat
                             seller_id    BIGINT        NOT NULL,       -- o anki satıcı (settlement için)
                             subtotal     NUMERIC(19,4) NOT NULL,       -- quantity * unit_price
                             created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_seller_id ON order_items(seller_id);