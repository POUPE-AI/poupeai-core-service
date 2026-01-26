CREATE TYPE job_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED');
CREATE TYPE destination_type AS ENUM ('BANK_ACCOUNT');

CREATE TABLE ingestion_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    profile_id UUID NOT NULL,
    status job_status NOT NULL DEFAULT 'PENDING',
    file_key_minio VARCHAR(255) NOT NULL,
    
    destination_entity_type destination_type NOT NULL,
    destination_entity_id UUID NOT NULL,

    summary JSONB,
    error_details TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    CONSTRAINT fk_ingestion_jobs_profiles FOREIGN KEY (profile_id) REFERENCES profiles(user_id)
);
