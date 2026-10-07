-- Baseline marker for the existing sales schema; no destructive DDL.
CREATE TABLE IF NOT EXISTS sales_schema_baseline (id SMALLINT PRIMARY KEY, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO sales_schema_baseline(id) VALUES (1) ON CONFLICT (id) DO NOTHING;
