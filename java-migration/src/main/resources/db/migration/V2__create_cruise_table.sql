-- V2: Create cruise table (maps to NCCRUISE DDM, Adabas DBID 012, File 041)

CREATE TABLE cruise (
    cruise_id          BIGINT       NOT NULL PRIMARY KEY,  -- CI: CRUISE-ID (N8.0) - Descriptor
    cruise_status      VARCHAR(1)   NOT NULL,              -- CK: CRUISE-STATUS (A1) - 0=planned,1=available,2=sold,3=removed
    start_date         BIGINT,                             -- CM: START-DATE (N8.0) - YYYYMMDD format - Descriptor
    start_time         BIGINT,                             -- CN: START-TIME (N6.0) - HHMMSS format
    end_date           BIGINT,                             -- CP: END-DATE (N8.0) - YYYYMMDD format - Descriptor
    end_time           BIGINT,                             -- CQ: END-TIME (N6.0) - HHMMSS format
    start_harbor       VARCHAR(20),                        -- CR: START-HARBOR (A20) - Descriptor
    destination_harbor VARCHAR(20),                        -- CS: DESTINATION-HARBOR (A20) - Descriptor
    id_yacht           BIGINT,                             -- CT: ID-YACHT (N8.0) - FK to yacht - Descriptor
    price_1w           DECIMAL(12, 3),                     -- CX: PRICE-1W (P10.3) - price for one week
    price_2w           DECIMAL(12, 3),                     -- CY: PRICE-2W (P10.3) - price for two weeks
    price_3w           DECIMAL(12, 3)                      -- CZ: PRICE-3W (P10.3) - price for three weeks
);

-- Indexes for Adabas descriptors (D flag)
CREATE INDEX idx_cruise_start_date ON cruise (start_date);
CREATE INDEX idx_cruise_end_date ON cruise (end_date);
CREATE INDEX idx_cruise_start_harbor ON cruise (start_harbor);
CREATE INDEX idx_cruise_destination_harbor ON cruise (destination_harbor);
CREATE INDEX idx_cruise_id_yacht ON cruise (id_yacht);

-- Note: Adabas has no enforced referential integrity. We use nullable FK
-- since orphaned references may exist in Adabas data.
-- ALTER TABLE cruise ADD CONSTRAINT fk_cruise_yacht FOREIGN KEY (id_yacht) REFERENCES yacht(yacht_id);
