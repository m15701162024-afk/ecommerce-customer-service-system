-- Default admin user (password: admin123)
INSERT INTO users (username, password, email, role, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('admin', '$2a$10$21R/Z2WxudS4QG0blcxSxeNBc.8zoSxZsw8P4nm5XZhcKOa7o.C42', 'admin@ecommerce.com', 'ADMIN', true, true, true, true);

-- Default user (password: user123)
INSERT INTO users (username, password, email, role, enabled, account_non_expired, account_non_locked, credentials_non_expired)
VALUES ('user', '$2a$10$EqKcp1WFKVQISheBxkVJceXI1MPqGkKnMGZ7/mXjJWZ7x.GdXmHHK', 'user@ecommerce.com', 'USER', true, true, true, true);
