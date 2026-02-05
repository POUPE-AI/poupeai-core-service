CREATE TABLE credit_cards (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL,
    institution_id BIGINT,
    name VARCHAR(50) NOT NULL,
    credit_limit DECIMAL(15, 2) NOT NULL,
    closing_day INT NOT NULL,
    due_day INT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT fk_credit_card_profile FOREIGN KEY (profile_id) REFERENCES profiles(user_id),
    CONSTRAINT fk_credit_card_institution FOREIGN KEY (institution_id) REFERENCES institutions(id),
    CONSTRAINT idx_credit_cards_profile_name_unique UNIQUE (profile_id, name)
);

CREATE INDEX idx_credit_cards_profile_id ON credit_cards(profile_id);
