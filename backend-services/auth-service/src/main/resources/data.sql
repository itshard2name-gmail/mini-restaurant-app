-- Insert Roles (Idempotent)
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER');

-- Insert Users (Idempotent)
INSERT IGNORE INTO users (username, password, enabled) 
VALUES 
('admin', '$2a$10$LbpBc6Ql2fLKRPJaav3upuMzxWiNvYQ1rbTqV8VCxTdMRpwSU2.au', true);

INSERT IGNORE INTO users (username, password, enabled) 
VALUES 
('customer', '$2a$10$LbpBc6Ql2fLKRPJaav3upuMzxWiNvYQ1rbTqV8VCxTdMRpwSU2.au', true);

-- Assign Roles (Idempotent: uses INSERT IGNORE to avoid duplicate pairs)
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name='ROLE_ADMIN';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name='ROLE_USER';

INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='customer' AND r.name='ROLE_USER';
