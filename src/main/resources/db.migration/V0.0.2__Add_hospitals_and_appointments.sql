-- Больницы: город, адрес, координаты для сортировки "ближайшие"
CREATE TABLE hospitals
(
    id         BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(100) NOT NULL,
    address    VARCHAR(500),
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_hospitals_city ON hospitals (city);

-- Записи на приём
CREATE TABLE appointments
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    user_id      BIGINT      NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    hospital_id  BIGINT      NOT NULL REFERENCES hospitals (id) ON DELETE CASCADE,
    scheduled_at TIMESTAMP   NOT NULL,
    status       VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at   TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_appointments_user_id ON appointments (user_id);
CREATE INDEX idx_appointments_hospital_id ON appointments (hospital_id);
CREATE INDEX idx_appointments_scheduled_at ON appointments (scheduled_at);

-- Права на больницы и записи
INSERT INTO permissions (name, description)
VALUES ('READ_HOSPITAL', 'Просмотр больниц и записей'),
       ('WRITE_APPOINTMENT', 'Создание/редактирование записей на приём');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.name = 'USER'
  AND p.name IN ('READ_HOSPITAL', 'WRITE_APPOINTMENT');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r,
     permissions p
WHERE r.name = 'ADMIN'
  AND p.name IN ('READ_HOSPITAL', 'WRITE_APPOINTMENT');

-- Тестовые больницы (для проверки)
INSERT INTO hospitals (name, city, address, latitude, longitude)
VALUES ('Городская больница №1', 'Алматы', 'ул. Жандосова, 55', 43.238949, 76.945465),
       ('Больница скорой помощи', 'Алматы', 'ул. Толе би, 81', 43.256666, 76.928611),
       ('Медицинский центр "Авиценна"', 'Алматы', 'ул. Розыбакиева, 247', 43.221388, 76.889444),
       ('Городская больница №1', 'Астана', 'пр. Республики, 34', 51.169392, 71.449074),
       ('Национальный научный центр', 'Астана', 'ул. Кенесары, 40', 51.160522, 71.470278);
