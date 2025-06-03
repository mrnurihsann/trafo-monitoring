-- PLN Transformer Monitoring Database Initialization Script
-- This script will be executed when PostgreSQL container starts for the first time

-- Create database if not exists (this is usually handled by POSTGRES_DB env var)
-- CREATE DATABASE IF NOT EXISTS trafo_monitoring;

-- Switch to the database
\c trafo_monitoring;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON DATABASE trafo_monitoring TO trafo_user;
GRANT ALL PRIVILEGES ON SCHEMA public TO trafo_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO trafo_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO trafo_user;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO trafo_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO trafo_user;

-- Create a comment
COMMENT ON DATABASE trafo_monitoring IS 'PLN Transformer Monitoring System Database';

-- Show completion message
DO $$
BEGIN
    RAISE NOTICE 'PLN Transformer Monitoring Database initialized successfully!';
END $$;
