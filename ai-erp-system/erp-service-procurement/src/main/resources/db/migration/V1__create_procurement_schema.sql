-- Procurement Service Database Schema
-- This migration creates all procurement tables

CREATE TABLE IF NOT EXISTS vendors (
    id SERIAL PRIMARY KEY,
    vendor_code VARCHAR(50) UNIQUE NOT NULL,
    vendor_name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    address TEXT NOT NULL,
    city VARCHAR(100),
    state VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    payment_terms VARCHAR(50) NOT NULL,
    performance_rating DECIMAL(3,2),
    delivery_performance_score INT,
    quality_score INT,
    response_score INT,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS purchase_requests (
    id SERIAL PRIMARY KEY,
    pr_number VARCHAR(100) UNIQUE NOT NULL,
    description TEXT NOT NULL,
    department_id VARCHAR(100),
    created_by VARCHAR(100),
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50),
    required_date TIMESTAMP,
    approver_comments TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS purchase_request_line_items (
    id SERIAL PRIMARY KEY,
    purchase_request_id BIGINT,
    item_code VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    quantity BIGINT NOT NULL,
    uom VARCHAR(20) NOT NULL,
    estimated_unit_price DECIMAL(10,2) NOT NULL,
    account_code VARCHAR(50),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS purchase_orders (
    id SERIAL PRIMARY KEY,
    po_number VARCHAR(100) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL,
    purchase_request_id BIGINT NOT NULL,
    po_date TIMESTAMP,
    due_date TIMESTAMP,
    expected_delivery_date TIMESTAMP,
    total_amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2),
    shipping_amount DECIMAL(12,2),
    discount_amount DECIMAL(12,2),
    net_amount DECIMAL(12,2),
    status VARCHAR(50) NOT NULL,
    shipping_address TEXT,
    special_instructions TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    FOREIGN KEY (purchase_request_id) REFERENCES purchase_requests(id)
);

CREATE TABLE IF NOT EXISTS purchase_order_line_items (
    id SERIAL PRIMARY KEY,
    purchase_order_id BIGINT,
    item_code VARCHAR(100) NOT NULL,
    item_description TEXT NOT NULL,
    ordered_quantity BIGINT NOT NULL,
    received_quantity BIGINT,
    invoiced_quantity BIGINT,
    uom VARCHAR(20) NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_amount DECIMAL(12,2),
    status VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS receipts (
    id SERIAL PRIMARY KEY,
    receipt_number VARCHAR(100) UNIQUE NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    receipt_date TIMESTAMP NOT NULL,
    received_by VARCHAR(100),
    quality_inspection VARCHAR(50),
    inspection_notes TEXT,
    status VARCHAR(50) NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id)
);

CREATE TABLE IF NOT EXISTS receipt_line_items (
    id SERIAL PRIMARY KEY,
    receipt_id BIGINT,
    po_line_item_id BIGINT,
    received_quantity BIGINT NOT NULL,
    serial_number VARCHAR(100),
    batch_number VARCHAR(100),
    quality_status VARCHAR(50),
    notes TEXT
);

CREATE TABLE IF NOT EXISTS invoices (
    id SERIAL PRIMARY KEY,
    invoice_number VARCHAR(100) UNIQUE NOT NULL,
    vendor_id BIGINT NOT NULL,
    purchase_order_id BIGINT NOT NULL,
    receipt_id BIGINT,
    invoice_date TIMESTAMP NOT NULL,
    due_date TIMESTAMP,
    invoice_amount DECIMAL(12,2) NOT NULL,
    tax_amount DECIMAL(12,2),
    net_amount DECIMAL(12,2),
    amount_paid DECIMAL(12,2),
    amount_due DECIMAL(12,2),
    status VARCHAR(50) NOT NULL,
    matching_status VARCHAR(50),
    discrepancy_notes TEXT,
    tenant_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (vendor_id) REFERENCES vendors(id),
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(id),
    FOREIGN KEY (receipt_id) REFERENCES receipts(id)
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_vendors_tenant ON vendors(tenant_id);
CREATE INDEX IF NOT EXISTS idx_pr_tenant ON purchase_requests(tenant_id);
CREATE INDEX IF NOT EXISTS idx_po_tenant ON purchase_orders(tenant_id);
CREATE INDEX IF NOT EXISTS idx_po_vendor ON purchase_orders(vendor_id);
CREATE INDEX IF NOT EXISTS idx_receipt_tenant ON receipts(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_tenant ON invoices(tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_vendor ON invoices(vendor_id);
