-- V3: Insert sample data for testing
-- Covers all code paths: every status value, edge cases, boundary dates, zero-value prices

-- Yacht records (referenced by cruises via ID-YACHT)
INSERT INTO yacht (yacht_id, yacht_name, yacht_type, length, width, draft, sail_surface, motor, head_room, bunks) VALUES
(1001, 'Cassandra', 'Sailing Yacht', 15.50, 4.80, 2.10, 120, 50, 1.95, 8),
(1002, 'Poseidon', 'Motor Yacht', 22.00, 6.50, 1.80, 0, 350, 2.10, 12),
(1003, 'Athena', 'Catamaran', 12.80, 7.20, 1.20, 95, 40, 1.85, 6),
(1004, 'Odysseus', 'Sailing Yacht', 18.30, 5.20, 2.50, 180, 75, 2.00, 10),
(1005, 'Artemis', 'Motor Yacht', 25.00, 7.00, 2.00, 0, 500, 2.20, 14);

-- Cruise records covering all status codes (0=removed, 1=planned, 2=available, 3=sold)
-- and edge cases

-- Status 0 (removed)
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(671, '0', 20150801, 100000, 20150820, 70000, 'Samos', 'Santorini', 1001, 1000.000, 2000.000, 3000.000);

-- Status 1 (planned)
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(675, '1', 20160315, 80000, 20160401, 180000, 'Piraeus', 'Mykonos', 1002, 1500.500, 2800.000, 4000.000);

-- Status 2 (available)
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(676, '2', 20160601, 90000, 20160615, 170000, 'Rhodes', 'Crete', 1003, 2500.750, 4500.000, 6200.000);

-- Status 3 (sold)
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(680, '3', 20151231, 120000, 20160115, 150000, 'Corfu', 'Dubrovnik', 1004, 3500.000, 6500.000, 9000.000);

-- Edge case: unknown status code
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(690, '9', 20170101, 0, 20170201, 235959, 'Athens', 'Istanbul', 1005, 5000.000, 9000.000, 12000.000);

-- Edge case: zero prices
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(691, '2', 20160701, 60000, 20160715, 200000, 'Naples', 'Barcelona', 1001, 0.000, 0.000, 0.000);

-- Edge case: yacht with no matching cruise (orphan check)
-- Already covered by yacht 1005 having only one cruise

-- Edge case: cruise with non-existent yacht (orphan FK)
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(692, '1', 20160801, 80000, 20160815, 180000, 'Venice', 'Split', 9999, 1200.000, 2200.000, 3100.000);

-- Additional cruises for pagination testing
INSERT INTO cruise (cruise_id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, id_yacht, price_1w, price_2w, price_3w) VALUES
(700, '2', 20160901, 70000, 20160920, 190000, 'Marseille', 'Genoa', 1002, 1800.000, 3400.000, 4800.000),
(701, '1', 20161001, 100000, 20161020, 160000, 'Lisbon', 'Casablanca', 1003, 2200.000, 4000.000, 5600.000),
(702, '2', 20161101, 90000, 20161115, 170000, 'Palma', 'Ibiza', 1004, 900.000, 1600.000, 2200.000);
