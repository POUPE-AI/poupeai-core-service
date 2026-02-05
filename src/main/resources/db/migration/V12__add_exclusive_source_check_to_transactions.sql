ALTER TABLE transactions
    ADD CONSTRAINT ck_transaction_source_exclusivity
        CHECK (
            (bank_account_id IS NOT NULL AND credit_card_id IS NULL) OR
            (bank_account_id IS NULL AND credit_card_id IS NOT NULL)
            );