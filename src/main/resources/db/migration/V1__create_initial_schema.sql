DROP TABLE IF EXISTS Sessions;
DROP TABLE IF EXISTS Locations;
DROP TABLE IF EXISTS Users;

CREATE TABLE Users (
    id SERIAL PRIMARY KEY,
    login VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE Locations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id INT NOT NULL,
    latitude DECIMAL(9, 6) NOT NULL,
    longitude DECIMAL(9, 6) NOT NULL,

    CONSTRAINT fk_locations_user
        FOREIGN KEY(user_id)
        REFERENCES Users(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_user_location_name UNIQUE (user_id, name)
);

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE Sessions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id INT NOT NULL,
    expires_at TIMESTAMP NOT NULL,

    CONSTRAINT fk_sessions_user
        FOREIGN KEY(user_id)
        REFERENCES Users(id)
        ON DELETE CASCADE
);