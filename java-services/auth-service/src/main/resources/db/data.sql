-- Default admin user (password: admin123)
-- BCrypt hash generated with strength 10
INSERT INTO users (username, password, email, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZRGdjGj/n3.rsQ5pPjZ5yVlWK5WAe', 'admin@ecommerce.com', 'ADMIN', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Default user (password: user123)
INSERT INTO users (username, password, email, role, enabled, account_non_expired, account_non_locked, credentials_non_expired, created_at, updated_at)
VALUES ('user', '$2a$10$EqKcp1WFKVQISheBxkVJceXI1MPqGkKnMGZ7/mXjJWZ7x.GdXmHHK', 'user@ecommerce.com', 'USER', TRUE, TRUE, TRUE, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
