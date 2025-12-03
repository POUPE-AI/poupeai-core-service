CREATE TYPE category_type AS ENUM ('INCOME', 'EXPENSE');

CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    color_hex VARCHAR(7) DEFAULT '#FFFFFF',
    icon_name VARCHAR(50),
    type category_type NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_category_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_categories_profile_name_unique 
    ON categories(profile_id, name);

CREATE INDEX idx_categories_profile_id ON categories(profile_id);