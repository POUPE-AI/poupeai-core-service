CREATE TABLE goals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    name VARCHAR(100) NOT NULL,
    goal_amount DECIMAL(15, 2) NOT NULL,
    target_date DATE,
    completed_at DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_goal_profile FOREIGN KEY (profile_id) 
        REFERENCES profiles(user_id) ON DELETE CASCADE
);

CREATE INDEX idx_goals_profile_id ON goals(profile_id);

CREATE TABLE goal_deposits (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_id UUID NOT NULL,
    deposit_amount DECIMAL(15, 2) NOT NULL,
    deposit_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT fk_goal_deposit_goal FOREIGN KEY (goal_id) 
        REFERENCES goals(id) ON DELETE CASCADE
);

CREATE INDEX idx_goal_deposits_goal_id ON goal_deposits(goal_id);
