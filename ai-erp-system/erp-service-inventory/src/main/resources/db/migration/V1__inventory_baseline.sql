-- Non-destructive V1 baseline. Existing tables are owned by Hibernate until the
-- complete schema is versioned; these statements are safe for shared databases.
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS average_unit_cost NUMERIC(19,6) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS inventory_value NUMERIC(19,2) NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS stock_balances ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS stock_movements ADD COLUMN IF NOT EXISTS unit_cost NUMERIC(19,6);
CREATE TABLE IF NOT EXISTS inventory_cost_layers (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, product_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL, location_id BIGINT, source_movement_id BIGINT,
 quantity_received NUMERIC(19,3) NOT NULL, quantity_remaining NUMERIC(19,3) NOT NULL,
 unit_cost NUMERIC(19,6) NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_cost_layer_balance ON inventory_cost_layers(tenant_id,product_id,warehouse_id,location_id);
ALTER TABLE IF EXISTS stock_movements ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(200);
ALTER TABLE IF EXISTS stock_movements ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'POSTED';
ALTER TABLE IF EXISTS stock_movements ADD COLUMN IF NOT EXISTS created_by VARCHAR(200);
ALTER TABLE IF EXISTS stock_movements ADD COLUMN IF NOT EXISTS approved_by VARCHAR(200);
CREATE UNIQUE INDEX IF NOT EXISTS uk_stock_movement_tenant_idempotency
 ON stock_movements(tenant_id,idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE TABLE IF NOT EXISTS inventory_reconciliations (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, product_id BIGINT NOT NULL,
 warehouse_id BIGINT NOT NULL, location_id BIGINT, expected_quantity NUMERIC(19,3) NOT NULL,
 actual_quantity NUMERIC(19,3) NOT NULL, quantity_discrepancy NUMERIC(19,3) NOT NULL,
 expected_value NUMERIC(19,2) NOT NULL, actual_value NUMERIC(19,2) NOT NULL,
 value_discrepancy NUMERIC(19,2) NOT NULL, status VARCHAR(20) NOT NULL,
 notes VARCHAR(500), created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_inventory_reconciliation_scope
 ON inventory_reconciliations(tenant_id,product_id,warehouse_id,location_id,created_at);
CREATE TABLE IF NOT EXISTS inventory_approval_policies (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, operation VARCHAR(100) NOT NULL,
 min_amount NUMERIC(19,2) NOT NULL DEFAULT 0, maker_checker BOOLEAN NOT NULL DEFAULT TRUE,
 active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_inventory_approval_policy_scope
 ON inventory_approval_policies(tenant_id,operation,active);
