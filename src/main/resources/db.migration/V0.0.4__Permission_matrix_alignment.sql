-- Приведение прав к матрице H3 Healthcare

-- Новые права: больницы (Create/Update/Delete), просмотр всех пользователей, отмена записи
INSERT INTO permissions (name, description)
VALUES ('WRITE_HOSPITAL', 'Создание/редактирование/удаление больниц'),
       ('READ_ALL_USERS', 'Просмотр списка всех пользователей'),
       ('CANCEL_APPOINTMENT', 'Отмена записи на приём (своей или любой)');

-- WORKER: просмотр больниц, создание записи (по матрице)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'WORKER' AND p.name IN ('READ_HOSPITAL', 'WRITE_APPOINTMENT')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- ADMIN: управление больницами, просмотр всех пользователей
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN' AND p.name IN ('WRITE_HOSPITAL', 'READ_ALL_USERS')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- Отмена записи: USER, WORKER, ADMIN (USER может отменять только свою — проверка в API)
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'USER'  AND p.name = 'CANCEL_APPOINTMENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'WORKER' AND p.name = 'CANCEL_APPOINTMENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p WHERE r.name = 'ADMIN'  AND p.name = 'CANCEL_APPOINTMENT'
ON CONFLICT (role_id, permission_id) DO NOTHING;
