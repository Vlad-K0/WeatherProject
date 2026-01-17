-- Миграция V2: Добавление region и country, обновление уникального ключа
-- Позволяет добавлять города с одинаковым названием но разными координатами

-- Добавляем новые колонки
ALTER TABLE Locations ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE Locations ADD COLUMN IF NOT EXISTS country VARCHAR(100);

-- Удаляем старый уникальный ключ (user_id, name)
ALTER TABLE Locations DROP CONSTRAINT IF EXISTS uk_user_location_name;

-- Создаем новый уникальный ключ (user_id, name, latitude, longitude)
-- Это позволяет иметь "Moscow, Russia" и "Moscow, USA" для одного пользователя
ALTER TABLE Locations ADD CONSTRAINT uk_user_location_coords 
    UNIQUE (user_id, name, latitude, longitude);
