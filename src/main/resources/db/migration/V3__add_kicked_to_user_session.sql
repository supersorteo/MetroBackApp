ALTER TABLE user_session ADD COLUMN IF NOT EXISTS kicked boolean NOT NULL DEFAULT false;
