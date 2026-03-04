package com.ntcruise.regression;

import com.ntcruise.dto.CruiseDetailDto;
import com.ntcruise.dto.CruiseFormDto;
import com.ntcruise.exception.CruiseNotFoundException;
import com.ntcruise.service.CruiseFindService;
import com.ntcruise.service.CruiseFormService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests: parallel-run validation comparing Java outputs against
 * documented Natural outputs for the same inputs.
 *
 * Each test case documents:
 * 1. The Natural input (e.g., cruise ID entered in NCINMAPP map)
 * 2. The expected Natural output (from NCFINDCR subprogram via NC-PARMS PDA)
 * 3. The field-by-field comparison between Natural and Java outputs
 *
 * These tests validate functional equivalence between the original Natural
 * NTCRUISE application and the migrated Java Spring Boot application.
 */
@SpringBootTest
@ActiveProfiles("test")
class NaturalParallelRunTest {

    @Autowired
    private CruiseFindService cruiseFindService;

    @Autowired
    private CruiseFormService cruiseFormService;

    // ==========================================
    // Test Case 1: Cruise 671 - Status "removed" (code '0')
    // ==========================================
    // Natural Input: Enter cruise ID 671 in NCINMAPP map (#CR-ID-FIND = 671)
    // Natural Process: CALLNAT 'NCFINDCR' NC-PARMS
    // Natural Output: NC-PARMS populated with formatted cruise data
    @Test
    @DisplayName("Parallel Run: Cruise 671 - status removed, full field comparison")
    void testParallelRun_cruise671() {
        // Java execution
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(671L);

        // Field-by-field comparison against Natural output
        // Natural: MOVE NCCRUISE.CRUISE-ID TO #CR-ID -> 671
        assertEquals(671L, javaResult.getCruiseId(),
                "Natural #CR-ID = 671");

        // Natural: DECIDE ON FIRST VALUE '0' -> MOVE 'removed' TO #CR-STATUS
        assertEquals("removed", javaResult.getStatus(),
                "Natural #CR-STATUS = 'removed' (code '0')");

        // Natural: MOVE EDITED START-DATE (EM=9999'-'99'-'99) -> '2015-08-01'
        assertEquals("2015-08-01", javaResult.getStartDate(),
                "Natural #CR-SD = '2015-08-01'");

        // Natural: COMPRESS START-TIME 'h' -> '100000 h'
        assertEquals("100000 h", javaResult.getStartTime(),
                "Natural #CR-ST = '100000 h'");

        // Natural: MOVE EDITED END-DATE (EM=9999'-'99'-'99) -> '2015-08-20'
        assertEquals("2015-08-20", javaResult.getEndDate(),
                "Natural #CR-ED = '2015-08-20'");

        // Natural: COMPRESS END-TIME 'h' -> '70000 h'
        assertEquals("70000 h", javaResult.getEndTime(),
                "Natural #CR-ET = '70000 h'");

        // Natural: MOVE START-HARBOR TO #CR-FROMH -> 'Samos'
        assertEquals("Samos", javaResult.getFromHarbor(),
                "Natural #CR-FROMH = 'Samos'");

        // Natural: MOVE DESTINATION-HARBOR TO #CR-TOH -> 'Santorini'
        assertEquals("Santorini", javaResult.getToHarbor(),
                "Natural #CR-TOH = 'Santorini'");

        // Natural: MOVE EDITED PRICE-1W (EM=*EUR' 'ZZZZ9.99) -> 'EUR  1000.00'
        assertEquals("EUR  1000.00", javaResult.getPrice1w(),
                "Natural #CR-P1W = 'EUR  1000.00'");

        // Natural: MOVE EDITED PRICE-2W (EM=*EUR' 'ZZZZ9.99) -> 'EUR  2000.00'
        assertEquals("EUR  2000.00", javaResult.getPrice2w(),
                "Natural #CR-P2W = 'EUR  2000.00'");

        // Natural: MOVE EDITED PRICE-3W (EM=*EUR' 'ZZZZ9.99) -> 'EUR  3000.00'
        assertEquals("EUR  3000.00", javaResult.getPrice3w(),
                "Natural #CR-P3W = 'EUR  3000.00'");

        // Natural: FIND NCYACHT / MOVE YACHT-NAME TO #CR-YACHT-NAME -> 'Cassandra'
        assertEquals("Cassandra", javaResult.getYachtName(),
                "Natural #CR-YACHT-NAME = 'Cassandra'");
    }

    // ==========================================
    // Test Case 2: Cruise 675 - Status "planned" (code '1')
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 675 - status planned")
    void testParallelRun_cruise675() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(675L);

        assertEquals(675L, javaResult.getCruiseId(), "Natural #CR-ID = 675");
        assertEquals("planned", javaResult.getStatus(), "Natural #CR-STATUS = 'planned' (code '1')");
        assertEquals("2016-03-15", javaResult.getStartDate(), "Natural #CR-SD = '2016-03-15'");
        assertEquals("80000 h", javaResult.getStartTime(), "Natural #CR-ST = '80000 h'");
        assertEquals("2016-04-01", javaResult.getEndDate(), "Natural #CR-ED = '2016-04-01'");
        assertEquals("180000 h", javaResult.getEndTime(), "Natural #CR-ET = '180000 h'");
        assertEquals("Piraeus", javaResult.getFromHarbor(), "Natural #CR-FROMH = 'Piraeus'");
        assertEquals("Mykonos", javaResult.getToHarbor(), "Natural #CR-TOH = 'Mykonos'");
        assertEquals("EUR  1500.50", javaResult.getPrice1w(), "Natural #CR-P1W = 'EUR  1500.50'");
        assertEquals("Poseidon", javaResult.getYachtName(), "Natural #CR-YACHT-NAME = 'Poseidon'");
    }

    // ==========================================
    // Test Case 3: Cruise 676 - Status "available" (code '2')
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 676 - status available")
    void testParallelRun_cruise676() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(676L);

        assertEquals(676L, javaResult.getCruiseId(), "Natural #CR-ID = 676");
        assertEquals("available", javaResult.getStatus(), "Natural #CR-STATUS = 'available' (code '2')");
        assertEquals("2016-06-01", javaResult.getStartDate(), "Natural #CR-SD = '2016-06-01'");
        assertEquals("2016-06-15", javaResult.getEndDate(), "Natural #CR-ED = '2016-06-15'");
        assertEquals("Rhodes", javaResult.getFromHarbor(), "Natural #CR-FROMH = 'Rhodes'");
        assertEquals("Crete", javaResult.getToHarbor(), "Natural #CR-TOH = 'Crete'");
        assertEquals("EUR  2500.75", javaResult.getPrice1w(), "Natural #CR-P1W = 'EUR  2500.75'");
        assertEquals("Athena", javaResult.getYachtName(), "Natural #CR-YACHT-NAME = 'Athena'");
    }

    // ==========================================
    // Test Case 4: Cruise 680 - Status "sold" (code '3'), year-end boundary
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 680 - status sold, year boundary Dec 31 -> Jan 15")
    void testParallelRun_cruise680() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(680L);

        assertEquals(680L, javaResult.getCruiseId(), "Natural #CR-ID = 680");
        assertEquals("sold", javaResult.getStatus(), "Natural #CR-STATUS = 'sold' (code '3')");
        assertEquals("2015-12-31", javaResult.getStartDate(), "Natural #CR-SD = '2015-12-31' (year-end)");
        assertEquals("2016-01-15", javaResult.getEndDate(), "Natural #CR-ED = '2016-01-15' (year-start)");
        assertEquals("Corfu", javaResult.getFromHarbor(), "Natural #CR-FROMH = 'Corfu'");
        assertEquals("Dubrovnik", javaResult.getToHarbor(), "Natural #CR-TOH = 'Dubrovnik'");
        assertEquals("Odysseus", javaResult.getYachtName(), "Natural #CR-YACHT-NAME = 'Odysseus'");
    }

    // ==========================================
    // Test Case 5: Cruise 690 - Unknown status (code '9' = NONE case)
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 690 - unknown status code '9' (NONE case)")
    void testParallelRun_cruise690_unknownStatus() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(690L);

        assertEquals("unknown", javaResult.getStatus(),
                "Natural NONE case: MOVE 'unknown' TO #CR-STATUS");
    }

    // ==========================================
    // Test Case 6: Non-existent cruise (RESET NC-PARMS + ESCAPE ROUTINE)
    // ==========================================
    // Natural Input: Enter cruise ID 99999 in NCINMAPP map
    // Natural Output: RESET NC-PARMS (all fields zeroed), ESCAPE ROUTINE
    // NCINMAPP: REINPUT FULL 'Sorry - No Cruise found for Id'
    @Test
    @DisplayName("Parallel Run: Non-existent cruise - RESET NC-PARMS + ESCAPE ROUTINE")
    void testParallelRun_notFound() {
        CruiseNotFoundException ex = assertThrows(CruiseNotFoundException.class,
                () -> cruiseFindService.findCruiseById(99999L));

        assertTrue(ex.getMessage().contains("Sorry - No Cruise found for Id"),
                "Natural REINPUT message: 'Sorry - No Cruise found for Id'");
    }

    // ==========================================
    // Test Case 7: Cruise with orphan FK (no matching yacht)
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 692 - orphan yacht FK returns empty yacht name")
    void testParallelRun_cruise692_orphanFK() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(692L);

        // Natural: FIND NCYACHT with no match -> yacht name field stays empty
        assertEquals("", javaResult.getYachtName(),
                "Natural: no matching NCYACHT record -> empty #CR-YACHT-NAME");
    }

    // ==========================================
    // Test Case 8: Cruise with zero prices
    // ==========================================
    @Test
    @DisplayName("Parallel Run: Cruise 691 - zero prices formatted as EUR     0.00")
    void testParallelRun_cruise691_zeroPrices() {
        CruiseDetailDto javaResult = cruiseFindService.findCruiseById(691L);

        assertEquals("EUR     0.00", javaResult.getPrice1w(),
                "Natural EM=*EUR' 'ZZZZ9.99 with 0 -> 'EUR     0.00'");
        assertEquals("EUR     0.00", javaResult.getPrice2w(),
                "Natural EM=*EUR' 'ZZZZ9.99 with 0 -> 'EUR     0.00'");
        assertEquals("EUR     0.00", javaResult.getPrice3w(),
                "Natural EM=*EUR' 'ZZZZ9.99 with 0 -> 'EUR     0.00'");
    }

    // ==========================================
    // Test Case 9: NCWRFORP form output (hardcoded INIT values)
    // ==========================================
    // Natural: WRITE USING FORM 'NCDEFORM' with INIT values
    @Test
    @DisplayName("Parallel Run: NCWRFORP form output matches Natural INIT values exactly")
    void testParallelRun_formOutput() {
        CruiseFormDto javaResult = cruiseFormService.getFormData();

        assertNotNull(javaResult);
        assertEquals(12345678L, javaResult.getCruiseId(), "Natural #CR-ID INIT <12345678>");
        assertEquals("available", javaResult.getStatus(), "Natural #CR-STATUS INIT <'available'>");
        assertEquals("2015-08-20", javaResult.getStartDate(), "Natural #CR-SD INIT <'2015-08-20'>");
        assertEquals("10 h", javaResult.getStartTime(), "Natural #CR-ST INIT <'10 h'>");
        assertEquals("2015-08-01", javaResult.getEndDate(), "Natural #CR-ED INIT <'2015-08-01'>");
        assertEquals("7 h", javaResult.getEndTime(), "Natural #CR-ET INIT <'7 h'>");
        assertEquals("Samos", javaResult.getFromHarbor(), "Natural #CR-FROMH INIT <'Samos'>");
        assertEquals("Santorini", javaResult.getToHarbor(), "Natural #CR-TOH INIT <'Santorini'>");
        assertEquals("1000.00", javaResult.getPrice1w(), "Natural #CR-P1W INIT <'1000.00'>");
        assertEquals("2000.00", javaResult.getPrice2w(), "Natural #CR-P2W INIT <'2000.00'>");
        assertEquals("3000.00", javaResult.getPrice3w(), "Natural #CR-P3W INIT <'3000.00'>");
        assertEquals("Cassandra", javaResult.getYachtName(), "Natural #CR-YACHT-NAME INIT <'Cassandra'>");
    }
}
