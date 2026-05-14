CREATE TABLE sectors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    code VARCHAR(50) NOT NULL,
    hourly_rate DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sectors_code (code)
);

CREATE TABLE parking_spots (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sector_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    occupied BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (id),
    CONSTRAINT fk_parking_spots_sector FOREIGN KEY (sector_id) REFERENCES sectors (id),
    UNIQUE KEY uk_spot_sector_code (sector_id, code)
);

CREATE TABLE parking_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    license_plate VARCHAR(20) NOT NULL,
    sector_id BIGINT NULL,
    spot_id BIGINT NULL,
    status VARCHAR(20) NOT NULL,
    entry_at DATETIME NOT NULL,
    parked_at DATETIME NULL,
    exit_at DATETIME NULL,
    revenue DECIMAL(10,2) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_sessions_sector FOREIGN KEY (sector_id) REFERENCES sectors (id),
    CONSTRAINT fk_sessions_spot FOREIGN KEY (spot_id) REFERENCES parking_spots (id)
);
