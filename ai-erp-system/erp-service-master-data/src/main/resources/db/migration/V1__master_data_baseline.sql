-- Baseline marker for the existing master-data schema; no destructive DDL.
CREATE TABLE IF NOT EXISTS master_data_schema_baseline (id SMALLINT PRIMARY KEY, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO master_data_schema_baseline(id) VALUES (1) ON CONFLICT (id) DO NOTHING;
