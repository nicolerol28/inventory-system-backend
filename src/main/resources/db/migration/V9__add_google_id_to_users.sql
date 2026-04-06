-- V9__add_google_id_to_users
-- Adds google_id column to the users table

ALTER TABLE users ADD COLUMN google_id VARCHAR(255);
ALTER TABLE users ALTER COLUMN password DROP NOT NULL;