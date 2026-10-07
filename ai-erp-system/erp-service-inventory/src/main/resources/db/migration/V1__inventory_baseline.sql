-- Non-destructive V1 baseline. Existing tables are owned by Hibernate until the
-- complete schema is versioned; these statements are safe for shared databases.
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS average_unit_cost NUMERIC(19,6) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS inventory_value NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE TABLE IF NOT EXISTS inventory_cost_layers (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, product_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL, location_id BIGINT, source_movement_id BIGINT,
 quantity_received NUMERIC(19,3) NOT NULL, quantity_remaining NUMERIC(19,3) NOT NULL,
 unit_cost NUMERIC(19,6) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_cost_layer_balance ON inventory_cost_layers(tenant_id,product_id,warehouse_id,location_id);
