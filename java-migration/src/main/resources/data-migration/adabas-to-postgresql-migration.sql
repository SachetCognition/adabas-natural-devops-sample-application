-- ==========================================================================
-- Data Migration Script: Adabas (Natural) to PostgreSQL
-- ==========================================================================
-- This script migrates data from an Adabas database (DBID 012) to PostgreSQL.
--
-- Prerequisites:
-- 1. Export Adabas data to CSV using one of:
--    a. Natural READ/WRITE to work file (CSV)
--    b. ADAULD utility
--    c. Adabas REST API (if available)
--
-- 2. PostgreSQL schema must already exist (Flyway V1/V2 migrations applied)
--
-- 3. CSV files must be placed in a staging directory
-- ==========================================================================

-- Step 1: Create staging tables for raw Adabas data import
-- These match the exact Adabas field formats before transformation

CREATE TEMPORARY TABLE staging_yacht (
    yacht_id    BIGINT,       -- DB: N8.0
    yacht_name  VARCHAR(30),  -- DC: A30
    yacht_type  VARCHAR(30),  -- DD: A30
    length      DECIMAL(5,2), -- DF: P3.2
    width       DECIMAL(5,2), -- DG: P3.2
    draft       DECIMAL(5,2), -- DH: P3.2
    sail_surface DECIMAL(5,0),-- DI: P3.0
    motor       DECIMAL(5,0), -- DJ: P3.0
    head_room   DECIMAL(5,2), -- DK: P3.2
    bunks       DECIMAL(5,0)  -- DL: P3.0
);

CREATE TEMPORARY TABLE staging_cruise (
    cruise_id          BIGINT,        -- CI: N8.0
    cruise_status      VARCHAR(1),    -- CK: A1
    start_date         BIGINT,        -- CM: N8.0 (YYYYMMDD)
    start_time         BIGINT,        -- CN: N6.0 (HHMMSS)
    end_date           BIGINT,        -- CP: N8.0 (YYYYMMDD)
    end_time           BIGINT,        -- CQ: N6.0 (HHMMSS)
    start_harbor       VARCHAR(20),   -- CR: A20
    destination_harbor VARCHAR(20),   -- CS: A20
    id_yacht           BIGINT,        -- CT: N8.0
    price_1w           DECIMAL(12,3), -- CX: P10.3
    price_2w           DECIMAL(12,3), -- CY: P10.3
    price_3w           DECIMAL(12,3)  -- CZ: P10.3
);

-- Step 2: Load CSV data using PostgreSQL COPY command
-- Uncomment and adjust paths when executing against actual Adabas export files

-- COPY staging_yacht FROM '/path/to/adabas_export/ncyacht.csv'
--     WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- COPY staging_cruise FROM '/path/to/adabas_export/nccruise.csv'
--     WITH (FORMAT csv, HEADER true, DELIMITER ',');

-- Step 3: Transform and insert into target tables
-- Adabas field transformations:
--   N8.0 dates (YYYYMMDD) -> stored as BIGINT (transformation done in application layer)
--   N6.0 times (HHMMSS)   -> stored as BIGINT (transformation done in application layer)
--   P10.3 packed decimals  -> DECIMAL(12,3) (direct mapping)

-- Insert yachts first (referenced by cruises)
INSERT INTO yacht (yacht_id, yacht_name, yacht_type, length, width, draft,
                   sail_surface, motor, head_room, bunks)
SELECT yacht_id, yacht_name, yacht_type, length, width, draft,
       sail_surface, motor, head_room, bunks
FROM staging_yacht
ON CONFLICT (yacht_id) DO NOTHING;

-- Insert cruises
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time,
                    start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w)
SELECT cruise_id, cruise_status, start_date, start_time, end_date, end_time,
       start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w
FROM staging_cruise
ON CONFLICT (cruise_id) DO NOTHING;

-- Step 4: Validate row counts
DO $$
DECLARE
    yacht_count BIGINT;
    cruise_count BIGINT;
    staging_yacht_count BIGINT;
    staging_cruise_count BIGINT;
BEGIN
    SELECT COUNT(*) INTO yacht_count FROM yacht;
    SELECT COUNT(*) INTO cruise_count FROM cruise;
    SELECT COUNT(*) INTO staging_yacht_count FROM staging_yacht;
    SELECT COUNT(*) INTO staging_cruise_count FROM staging_cruise;

    RAISE NOTICE 'Migration Validation:';
    RAISE NOTICE '  Staging yacht records: %', staging_yacht_count;
    RAISE NOTICE '  Target yacht records:  %', yacht_count;
    RAISE NOTICE '  Staging cruise records: %', staging_cruise_count;
    RAISE NOTICE '  Target cruise records:  %', cruise_count;

    IF yacht_count < staging_yacht_count THEN
        RAISE WARNING 'YACHT: Target has fewer records than staging (% < %)',
            yacht_count, staging_yacht_count;
    END IF;

    IF cruise_count < staging_cruise_count THEN
        RAISE WARNING 'CRUISE: Target has fewer records than staging (% < %)',
            cruise_count, staging_cruise_count;
    END IF;

    RAISE NOTICE 'Migration validation complete.';
END $$;

-- Step 5: Verify referential integrity
SELECT COUNT(*) AS orphaned_cruise_yacht_refs
FROM cruise c
LEFT JOIN yacht y ON c.id_yacht = y.yacht_id
WHERE c.id_yacht IS NOT NULL AND y.yacht_id IS NULL;

-- Step 6: Clean up staging tables
DROP TABLE IF EXISTS staging_yacht;
DROP TABLE IF EXISTS staging_cruise;
