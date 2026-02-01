CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    profile_id UUID REFERENCES profiles(user_id) ON DELETE SET NULL,
    action_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    changes JSONB,
    source_ip VARCHAR(45),
    correlation_id UUID,
    service_name VARCHAR(50) NOT NULL
);

CREATE INDEX idx_audit_logs_profile_id ON audit_logs(profile_id);
CREATE INDEX idx_audit_logs_action_time ON audit_logs(action_time);
CREATE INDEX idx_audit_logs_entity_type ON audit_logs(entity_type);
CREATE INDEX idx_audit_logs_correlation_id ON audit_logs(correlation_id);
