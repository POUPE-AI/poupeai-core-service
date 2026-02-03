ALTER TABLE categories
    DROP CONSTRAINT fk_category_profile,
    ADD CONSTRAINT fk_category_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;

ALTER TABLE goals
    DROP CONSTRAINT fk_goal_profile,
    ADD CONSTRAINT fk_goal_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;

ALTER TABLE credit_cards
    DROP CONSTRAINT fk_credit_card_profile,
    ADD CONSTRAINT fk_credit_card_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;

ALTER TABLE bank_accounts
    DROP CONSTRAINT fk_bank_account_profile,
    ADD CONSTRAINT fk_bank_account_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;

ALTER TABLE transactions
    DROP CONSTRAINT fk_transaction_profile,
    ADD CONSTRAINT fk_transaction_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;

ALTER TABLE ingestion_jobs
    DROP CONSTRAINT fk_ingestion_jobs_profiles,
    ADD CONSTRAINT fk_ingestion_jobs_profiles FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE;
