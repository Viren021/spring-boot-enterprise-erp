CREATE TABLE IF NOT EXISTS processed_business_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(100) NOT NULL,
    event_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL,
    attempt_count INT NOT NULL DEFAULT 1,
    last_failed_at TIMESTAMP,
    last_error TEXT,
    CONSTRAINT uk_processed_event_tenant_id UNIQUE (tenant_id, event_id)
);

CREATE TABLE IF NOT EXISTS procurement_outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    topic VARCHAR(150) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP,
    dead_lettered_at TIMESTAMP,
    last_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON procurement_outbox_events(status, next_attempt_at);
