-- Migrate sectors table: rename columns, add new columns
ALTER TABLE sectors
    RENAME COLUMN code TO name,
    RENAME COLUMN hourly_rate TO base_price,
    ADD COLUMN max_capacity INT NOT NULL DEFAULT 0,
    ADD COLUMN current_occupation INT NOT NULL DEFAULT 0,
    ADD COLUMN closed BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE sectors
    MODIFY COLUMN base_price DECIMAL(10, 2) NOT NULL;

-- Migrate parking_spots table: rename code -> external_id, add lat/lng
ALTER TABLE parking_spots
    RENAME COLUMN code TO external_id,
    ADD COLUMN lat DOUBLE NULL,
    ADD COLUMN lng DOUBLE NULL;

-- Rename parking_sessions table to vehicle_stays and migrate columns
RENAME TABLE parking_sessions TO vehicle_stays;

ALTER TABLE vehicle_stays
    RENAME COLUMN entry_at TO entry_time,
    RENAME COLUMN parked_at TO parked_time,
    RENAME COLUMN exit_at TO exit_time,
    RENAME COLUMN revenue TO total_amount,
    ADD COLUMN applied_hourly_price DECIMAL(10, 2) NULL,
    MODIFY COLUMN entry_time DATETIME(6) NOT NULL,
    MODIFY COLUMN parked_time DATETIME(6) NULL,
    MODIFY COLUMN exit_time DATETIME(6) NULL;
-- Note: Hibernate 6 maps java.time.Instant to DATETIME(6) in MySQL (not TIMESTAMP),
-- storing values in UTC. DATETIME(6) avoids MySQL TIMESTAMP year-2038 limitations.

-- Useful indexes on vehicle_stays
ALTER TABLE vehicle_stays
    ADD INDEX idx_vehicle_stays_license_plate (license_plate),
    ADD INDEX idx_vehicle_stays_sector_exit_time (sector_id, exit_time);

-- Create revenues table
CREATE TABLE revenues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sector_id BIGINT NOT NULL,
    date DATE NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_revenues_sector FOREIGN KEY (sector_id) REFERENCES sectors (id),
    UNIQUE KEY uk_revenues_sector_date (sector_id, date)
);
