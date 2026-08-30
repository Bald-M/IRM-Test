ALTER TABLE user_verification
    ADD COLUMN code_hash VARCHAR(60);

ALTER TABLE user_verification
    ADD COLUMN failed_attempts INT NOT NULL DEFAULT 0;

ALTER TABLE user_verification
    ADD COLUMN last_sent_date DATETIME(6);

CREATE INDEX idx_user_verification_last_sent ON user_verification (last_sent_date);

-- Existing plaintext challenges cannot be migrated safely; require users to request a new OTP.
UPDATE user_verification
SET code = NULL,
    status = 'Inactive'
WHERE code IS NOT NULL;
