-- V1: Create yacht table (maps to NCYACHT DDM, Adabas DBID 012, File 042)
-- Must be created before cruise table due to foreign key reference

CREATE TABLE yacht (
    yacht_id    BIGINT       NOT NULL PRIMARY KEY,  -- DB: YACHT-ID (N8.0) - Descriptor
    yacht_name  VARCHAR(30)  NOT NULL,              -- DC: YACHT-NAME (A30) - Descriptor
    yacht_type  VARCHAR(30)  NOT NULL,              -- DD: YACHT-TYPE (A30) - Descriptor
    length      DECIMAL(5, 2),                      -- DF: LENGTH (P3.2) - in meters
    width       DECIMAL(5, 2),                      -- DG: WIDTH (P3.2) - in meters
    draft       DECIMAL(5, 2),                      -- DH: DRAFT (P3.2) - in meters
    sail_surface DECIMAL(5, 0),                     -- DI: SAIL-SURFACE (P3.0) - in square meters
    motor       DECIMAL(5, 0),                      -- DJ: MOTOR (P3.0) - output in HP
    head_room   DECIMAL(5, 2),                      -- DK: HEAD-ROOM (P3.2) - headroom in saloon in meters
    bunks       DECIMAL(5, 0)                       -- DL: BUNKS (P3.0) - number of bunks
);

-- Indexes for Adabas descriptors (D flag)
CREATE INDEX idx_yacht_name ON yacht (yacht_name);
CREATE INDEX idx_yacht_type ON yacht (yacht_type);
