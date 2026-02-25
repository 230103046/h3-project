-- Роль воркера (приём заявок пользователей)
INSERT INTO roles (name, description)
VALUES ('WORKER', 'Воркер: просмотр и приём заявок на приём');

-- Права воркера: список заявок, смена статуса (принять/подтвердить/отменить)
INSERT INTO permissions (name, description)
VALUES ('READ_APPOINTMENTS', 'Просмотр списка заявок на приём'),
       ('UPDATE_APPOINTMENT_STATUS', 'Смена статуса заявки (принять, подтвердить, отменить)');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.name = 'WORKER'
  AND p.name IN ('READ_APPOINTMENTS', 'UPDATE_APPOINTMENT_STATUS');

-- ADMIN тоже может управлять заявками
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('READ_APPOINTMENTS', 'UPDATE_APPOINTMENT_STATUS');

-- Тестовый воркер для проверки (логин: worker, пароль: worker)
INSERT INTO users (username, password, email, enabled)
VALUES ('worker', 'worker', 'worker@h3.kz', true)
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u,
     roles r
WHERE u.username = 'worker'
  AND r.name = 'WORKER'
ON CONFLICT (user_id, role_id) DO NOTHING;
