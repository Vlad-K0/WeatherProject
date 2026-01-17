ALTER TABLE Locations ADD COLUMN IF NOT EXISTS region VARCHAR(100);
ALTER TABLE Locations ADD COLUMN IF NOT EXISTS country VARCHAR(100);

ALTER TABLE Locations DROP CONSTRAINT IF EXISTS uk_user_location_name;

ALTER TABLE Locations ADD CONSTRAINT uk_user_location_coords 
    UNIQUE (user_id, name, latitude, longitude);
