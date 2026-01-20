CREATE TYPE invoice_status AS ENUM ('OPEN', 'CLOSED', 'PAID', 'PARTIALLY_PAID', 'OVERDUE');

CREATE TABLE invoices (
    id UUID PRIMARY KEY,
    credit_card_id UUID NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    closing_date DATE NOT NULL,
    due_date DATE NOT NULL,
    total_amount DECIMAL(15, 2) DEFAULT 0.00,
    paid_amount DECIMAL(15, 2) DEFAULT 0.00,
    status invoice_status NOT NULL DEFAULT 'OPEN',
    due_soon_notification_sent BOOLEAN DEFAULT FALSE,
    overdue_notification_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_invoice_credit_card FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id),
    CONSTRAINT idx_invoices_credit_card_month_year_unique UNIQUE (credit_card_id, month, year)
);

CREATE INDEX idx_invoices_credit_card_id ON invoices(credit_card_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);
