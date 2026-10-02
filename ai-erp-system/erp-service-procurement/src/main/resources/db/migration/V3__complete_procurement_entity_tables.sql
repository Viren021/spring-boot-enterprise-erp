-- Complete tables for entities introduced after the initial procurement schema.
-- CREATE IF NOT EXISTS keeps this migration safe for databases already provisioned.
CREATE TABLE IF NOT EXISTS invoice_line_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT,
    purchase_order_line_item_id BIGINT,
    item_code VARCHAR(255) NOT NULL,
    invoiced_quantity BIGINT NOT NULL,
    unit_price DECIMAL(19,2) NOT NULL,
    line_amount DECIMAL(19,2),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (purchase_order_line_item_id) REFERENCES purchase_order_line_items(id)
);

CREATE TABLE IF NOT EXISTS procurement_audit_events (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(255) NOT NULL,
    document_type VARCHAR(255) NOT NULL,
    document_id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    actor VARCHAR(255),
    details VARCHAR(255),
    occurred_at TIMESTAMP NOT NULL
);
