package com.ntcruise.migration;

import com.ntcruise.model.Cruise;
import com.ntcruise.model.Yacht;
import com.ntcruise.repository.CruiseRepository;
import com.ntcruise.repository.YachtRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Data migration validation tests.
 * Verifies the Adabas-to-PostgreSQL data migration integrity.
 *
 * Tests:
 * 1. Row count comparison per table
 * 2. Spot-check queries for representative records
 * 3. Referential integrity (no orphaned FKs where expected)
 * 4. NULL handling consistency
 */
@SpringBootTest
@ActiveProfiles("test")
class DataMigrationValidationTest {

    @Autowired
    private CruiseRepository cruiseRepository;

    @Autowired
    private YachtRepository yachtRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Row count comparison ---

    @Test
    @DisplayName("Yacht table has expected record count from sample data migration")
    void testYachtRowCount() {
        long count = yachtRepository.count();
        // V3 inserts 5 yacht records
        assertEquals(5, count, "Yacht table should have 5 records from sample data");
    }

    @Test
    @DisplayName("Cruise table has expected record count from sample data migration")
    void testCruiseRowCount() {
        long count = cruiseRepository.count();
        // V3 inserts 10 cruise records
        assertEquals(10, count, "Cruise table should have 10 records from sample data");
    }

    // --- Spot-check queries ---

    @Test
    @DisplayName("Spot-check: Yacht 1001 (Cassandra) has correct field values")
    void testSpotCheck_yacht1001() {
        Optional<Yacht> yachtOpt = yachtRepository.findById(1001L);
        assertTrue(yachtOpt.isPresent(), "Yacht 1001 should exist");

        Yacht yacht = yachtOpt.get();
        assertEquals("Cassandra", yacht.getYachtName());
        assertEquals("Sailing Yacht", yacht.getYachtType());
        assertNotNull(yacht.getLength());
        assertNotNull(yacht.getWidth());
    }

    @Test
    @DisplayName("Spot-check: Yacht 1002 (Poseidon) has correct field values")
    void testSpotCheck_yacht1002() {
        Optional<Yacht> yachtOpt = yachtRepository.findById(1002L);
        assertTrue(yachtOpt.isPresent(), "Yacht 1002 should exist");

        Yacht yacht = yachtOpt.get();
        assertEquals("Poseidon", yacht.getYachtName());
        assertEquals("Motor Yacht", yacht.getYachtType());
    }

    @Test
    @DisplayName("Spot-check: Cruise 671 has correct field values matching Adabas source")
    void testSpotCheck_cruise671() {
        Optional<Cruise> cruiseOpt = cruiseRepository.findById(671L);
        assertTrue(cruiseOpt.isPresent(), "Cruise 671 should exist");

        Cruise cruise = cruiseOpt.get();
        assertEquals("0", cruise.getCruiseStatus());
        assertEquals(20150801L, cruise.getStartDate());
        assertEquals(100000L, cruise.getStartTime());
        assertEquals(20150820L, cruise.getEndDate());
        assertEquals(70000L, cruise.getEndTime());
        assertEquals("Samos", cruise.getStartHarbor());
        assertEquals("Santorini", cruise.getDestinationHarbor());
        assertEquals(1001L, cruise.getIdYacht());
    }

    @Test
    @DisplayName("Spot-check: Cruise 675 has correct field values")
    void testSpotCheck_cruise675() {
        Optional<Cruise> cruiseOpt = cruiseRepository.findById(675L);
        assertTrue(cruiseOpt.isPresent(), "Cruise 675 should exist");

        Cruise cruise = cruiseOpt.get();
        assertEquals("1", cruise.getCruiseStatus());
        assertEquals("Piraeus", cruise.getStartHarbor());
        assertEquals("Mykonos", cruise.getDestinationHarbor());
        assertEquals(1002L, cruise.getIdYacht());
    }

    // --- Referential integrity ---

    @Test
    @DisplayName("Referential integrity: Count cruises with valid yacht references")
    void testReferentialIntegrity_validYachtRefs() {
        // Count cruises that have a yacht ID pointing to an existing yacht
        Long validRefCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise c " +
                "INNER JOIN yacht y ON c.id_yacht = y.yacht_id",
                Long.class);

        assertNotNull(validRefCount);
        // 9 out of 10 cruises have valid yacht references (692 has orphan FK 9999)
        assertEquals(9L, validRefCount,
                "9 cruises should have valid yacht references");
    }

    @Test
    @DisplayName("Referential integrity: Identify orphaned yacht FKs")
    void testReferentialIntegrity_orphanedFKs() {
        // Count cruises with yacht IDs that don't match any yacht
        Long orphanCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise c " +
                "LEFT JOIN yacht y ON c.id_yacht = y.yacht_id " +
                "WHERE c.id_yacht IS NOT NULL AND y.yacht_id IS NULL",
                Long.class);

        assertNotNull(orphanCount);
        // Cruise 692 has id_yacht=9999 which doesn't exist in yacht table
        assertEquals(1L, orphanCount,
                "1 cruise should have an orphaned yacht FK (cruise 692 -> yacht 9999)");
    }

    // --- NULL handling ---

    @Test
    @DisplayName("NULL handling: All cruise records have non-null status")
    void testNullHandling_cruiseStatus() {
        Long nullStatusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise WHERE cruise_status IS NULL",
                Long.class);

        assertNotNull(nullStatusCount);
        assertEquals(0L, nullStatusCount,
                "No cruise records should have NULL status");
    }

    @Test
    @DisplayName("NULL handling: All yacht records have non-null names")
    void testNullHandling_yachtName() {
        Long nullNameCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM yacht WHERE yacht_name IS NULL",
                Long.class);

        assertNotNull(nullNameCount);
        assertEquals(0L, nullNameCount,
                "No yacht records should have NULL names");
    }

    @Test
    @DisplayName("NULL handling: Price fields allow zero but not null for sample data")
    void testNullHandling_prices() {
        Long nullPriceCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise WHERE price_1w IS NULL OR price_2w IS NULL OR price_3w IS NULL",
                Long.class);

        assertNotNull(nullPriceCount);
        assertEquals(0L, nullPriceCount,
                "All sample cruise records should have non-null prices");
    }

    // --- Data type validation ---

    @Test
    @DisplayName("Date format: All start_date values are valid YYYYMMDD")
    void testDateFormat_startDate() {
        Long invalidDateCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise WHERE start_date IS NOT NULL " +
                "AND (start_date < 19000101 OR start_date > 29991231)",
                Long.class);

        assertNotNull(invalidDateCount);
        assertEquals(0L, invalidDateCount,
                "All start_date values should be valid YYYYMMDD format");
    }

    @Test
    @DisplayName("Status values: All cruise_status codes are single characters")
    void testStatusValues() {
        Long invalidStatusCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cruise WHERE LENGTH(cruise_status) != 1",
                Long.class);

        assertNotNull(invalidStatusCount);
        assertEquals(0L, invalidStatusCount,
                "All cruise_status values should be single characters");
    }
}
