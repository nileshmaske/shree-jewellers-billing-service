CREATE TABLE invoices (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 invoice_number VARCHAR(40) NOT NULL UNIQUE,
 customer_name VARCHAR(160) NOT NULL,
 customer_phone VARCHAR(25),
 status VARCHAR(25) NOT NULL,
 subtotal DECIMAL(16,2) NOT NULL,
 discount DECIMAL(16,2) NOT NULL,
 taxable_amount DECIMAL(16,2) NOT NULL,
 gst_rate DECIMAL(5,2) NOT NULL,
 gst_amount DECIMAL(16,2) NOT NULL,
 grand_total DECIMAL(16,2) NOT NULL,
 created_by VARCHAR(160) NOT NULL,
 created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);
CREATE TABLE invoice_items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 invoice_id BIGINT NOT NULL,
 product_id BIGINT NOT NULL,
 sku VARCHAR(60) NOT NULL,
 description VARCHAR(200) NOT NULL,
 quantity INT NOT NULL,
 unit_price DECIMAL(16,2) NOT NULL,
 line_total DECIMAL(16,2) NOT NULL,
 CONSTRAINT fk_invoice_items_invoice FOREIGN KEY(invoice_id) REFERENCES invoices(id)
);
CREATE INDEX idx_invoice_created_at ON invoices(created_at);
CREATE INDEX idx_invoice_status ON invoices(status);
