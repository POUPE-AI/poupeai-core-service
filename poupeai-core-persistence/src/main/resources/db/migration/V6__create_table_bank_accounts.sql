CREATE TABLE bank_accounts (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    institution_id BIGINT,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    initial_balance DECIMAL(15, 2) DEFAULT 0.00,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_bank_account_profile FOREIGN KEY (profile_id) REFERENCES profiles(user_id),
    CONSTRAINT fk_bank_account_institution FOREIGN KEY (institution_id) REFERENCES institutions(id),
    CONSTRAINT idx_bank_accounts_profile_name_unique UNIQUE (profile_id, name)
);

CREATE INDEX idx_bank_accounts_profile_id ON bank_accounts(profile_id);
