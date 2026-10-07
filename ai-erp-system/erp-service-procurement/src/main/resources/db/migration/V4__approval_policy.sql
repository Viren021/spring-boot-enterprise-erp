CREATE TABLE IF NOT EXISTS approval_policies (
 id BIGSERIAL PRIMARY KEY, tenant_id VARCHAR(100) NOT NULL, document_type VARCHAR(100) NOT NULL,
 min_amount NUMERIC(19,2) NOT NULL DEFAULT 0, max_amount NUMERIC(19,2), role VARCHAR(100),
 department_id VARCHAR(100), maker_checker BOOLEAN NOT NULL DEFAULT TRUE,
 active BOOLEAN NOT NULL DEFAULT TRUE, approval_level INTEGER NOT NULL DEFAULT 1
);
CREATE INDEX IF NOT EXISTS idx_approval_policy_scope ON approval_policies(tenant_id,document_type,active);
