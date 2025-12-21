-- Insert Roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT INTO roles (name) VALUES ('ROLE_USER');

-- Insert Users (password is '123456' BCrypt hashed)
-- Hash generated via app: $2a$10$LbpBc6Ql2fLKRPJaav3upuMzxWiNvYQ1rbTqV8VCxTdMRpwSU2.au
INSERT INTO users (username, password, enabled) 
VALUES 
('admin', '$2a$10$LbpBc6Ql2fLKRPJaav3upuMzxWiNvYQ1rbTqV8VCxTdMRpwSU2.au', true);

INSERT INTO users (username, password, enabled) 
VALUES 
('customer', '$2a$10$LbpBc6Ql2fLKRPJaav3upuMzxWiNvYQ1rbTqV8VCxTdMRpwSU2.au', true);

-- Assign Roles
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name='ROLE_ADMIN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='admin' AND r.name='ROLE_USER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r WHERE u.username='customer' AND r.name='ROLE_USER';
