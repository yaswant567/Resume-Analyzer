-- Enable UUID generation
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Users table
CREATE TABLE users (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                VARCHAR(255) NOT NULL UNIQUE,
    password             VARCHAR(255) NOT NULL,
    name                 VARCHAR(255) NOT NULL,
    daily_analysis_count INTEGER NOT NULL DEFAULT 0,
    last_analysis_date   DATE,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_email ON users (email);

-- Analyses table
CREATE TABLE analyses (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    resume_text      TEXT,
    job_description  TEXT NOT NULL,
    match_score      INTEGER CHECK (match_score >= 0 AND match_score <= 100),
    matched_keywords JSONB NOT NULL DEFAULT '[]'::jsonb,
    missing_keywords JSONB NOT NULL DEFAULT '[]'::jsonb,
    strengths        JSONB NOT NULL DEFAULT '[]'::jsonb,
    improvements     JSONB NOT NULL DEFAULT '[]'::jsonb,
    summary          TEXT,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED')),
    error_message    TEXT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_analyses_user_id ON analyses (user_id);
CREATE INDEX idx_analyses_status ON analyses (status);
CREATE INDEX idx_analyses_created_at ON analyses (created_at DESC);
