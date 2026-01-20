CREATE TABLE invoice_payments (
  id BIGSERIAL PRIMARY KEY,
  invoice_id UUID NOT NULL,
  payment_transaction_id UUID NOT NULL UNIQUE,
  amount DECIMAL(15, 2) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT fk_invoice_payment_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id),
  CONSTRAINT fk_invoice_payment_transaction FOREIGN KEY (payment_transaction_id) REFERENCES transactions(id),
  CONSTRAINT idx_invoice_payments_invoice_transaction_unique UNIQUE (invoice_id, payment_transaction_id)
);

CREATE INDEX idx_invoice_payments_invoice_id ON invoice_payments(invoice_id);
