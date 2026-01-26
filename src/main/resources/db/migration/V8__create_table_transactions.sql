CREATE TYPE transaction_type AS ENUM ('INCOME', 'EXPENSE');

CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    type transaction_type NOT NULL,
    transaction_date DATE NOT NULL,
    bank_account_id UUID,
    credit_card_id UUID,
    category_id UUID,
    invoice_id UUID,
    attachment_key VARCHAR(255),
    is_installment BOOLEAN DEFAULT FALSE,
    installment_number INT,
    total_installments INT,
    purchase_group_uuid UUID,
    original_statement_id VARCHAR(100),
    original_statement_description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_transaction_profile FOREIGN KEY (profile_id) REFERENCES profiles(user_id),
    CONSTRAINT fk_transaction_bank_account FOREIGN KEY (bank_account_id) REFERENCES bank_accounts(id),
    CONSTRAINT fk_transaction_credit_card FOREIGN KEY (credit_card_id) REFERENCES credit_cards(id),
    CONSTRAINT fk_transaction_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT fk_transaction_invoice FOREIGN KEY (invoice_id) REFERENCES invoices(id)
);

CREATE INDEX idx_transactions_profile_id ON transactions(profile_id);
CREATE INDEX idx_transactions_bank_account_id ON transactions(bank_account_id);
CREATE INDEX idx_transactions_credit_card_id ON transactions(credit_card_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_invoice_id ON transactions(invoice_id);
CREATE INDEX idx_transactions_transaction_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_purchase_group_uuid ON transactions(purchase_group_uuid);
CREATE INDEX idx_transactions_type ON transactions(type);
