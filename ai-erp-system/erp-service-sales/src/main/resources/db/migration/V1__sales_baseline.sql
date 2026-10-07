-- Baseline marker for the existing sales schema; no destructive DDL.
CREATE TABLE IF NOT EXISTS sales_schema_baseline (id SMALLINT PRIMARY KEY, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO sales_schema_baseline(id) VALUES (1) ON CONFLICT (id) DO NOTHING;
ALTER TABLE IF EXISTS sales_orders ADD COLUMN IF NOT EXISTS created_by VARCHAR(200);
ALTER TABLE IF EXISTS sales_orders ADD COLUMN IF NOT EXISTS approved_by VARCHAR(200);
CREATE TABLE IF NOT EXISTS sales_approval_policies (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, document_type VARCHAR(100) NOT NULL,
 min_amount NUMERIC(19,2) NOT NULL DEFAULT 0, max_amount NUMERIC(19,2),
 maker_checker BOOLEAN NOT NULL DEFAULT TRUE, active BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE INDEX IF NOT EXISTS idx_sales_approval_policy_scope
 ON sales_approval_policies(tenant_id,document_type,active);
