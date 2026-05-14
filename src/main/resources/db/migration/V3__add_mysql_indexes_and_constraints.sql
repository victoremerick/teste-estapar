ALTER TABLE sectors
    MODIFY COLUMN base_price DECIMAL(10, 2) NOT NULL;

ALTER TABLE parking_spots
    ADD CONSTRAINT uk_parking_spots_external_id UNIQUE (external_id),
    MODIFY COLUMN lat DOUBLE NULL,
    MODIFY COLUMN lng DOUBLE NULL,
    ADD INDEX idx_parking_spots_sector_id (sector_id);

-- DATETIME(6) columns below store UTC instants controlled by the application layer (java.time.Instant).
ALTER TABLE vehicle_stays
    MODIFY COLUMN entry_time DATETIME(6) NOT NULL,
    MODIFY COLUMN parked_time DATETIME(6) NULL,
    MODIFY COLUMN exit_time DATETIME(6) NULL;

ALTER TABLE vehicle_stays
    MODIFY COLUMN applied_hourly_price DECIMAL(10, 2) NULL,
    MODIFY COLUMN total_amount DECIMAL(10, 2) NULL;

ALTER TABLE vehicle_stays
    ADD INDEX idx_vehicle_stays_sector_id (sector_id),
    ADD INDEX idx_vehicle_stays_entry_time (entry_time),
    ADD INDEX idx_vehicle_stays_exit_time (exit_time),
    ADD INDEX idx_vehicle_stays_status (status);
