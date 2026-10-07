-- Baseline marker is intentionally non-destructive; legacy finance tables remain untouched.
CREATE TABLE IF NOT EXISTS finance_schema_baseline (id SMALLINT PRIMARY KEY, applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP);
INSERT INTO finance_schema_baseline(id) VALUES (1) ON CONFLICT (id) DO NOTHING;
ALTER TABLE IF EXISTS finance_journal_entries ADD COLUMN IF NOT EXISTS created_by VARCHAR(200);
ALTER TABLE IF EXISTS finance_journal_entries ADD COLUMN IF NOT EXISTS approved_by VARCHAR(200);
