package com.ntcruise.service;

import com.ntcruise.dto.CruiseDetailDto;
import com.ntcruise.exception.CruiseNotFoundException;
import com.ntcruise.model.Cruise;
import com.ntcruise.model.Yacht;
import com.ntcruise.repository.CruiseRepository;
import com.ntcruise.repository.YachtRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CruiseFindService.
 * Validates functional equivalence with NCFINDCR.NSN subprogram.
 */
@ExtendWith(MockitoExtension.class)
class CruiseFindServiceTest {

    @Mock
    private CruiseRepository cruiseRepository;

    @Mock
    private YachtRepository yachtRepository;

    private CruiseFindService cruiseFindService;

    @BeforeEach
    void setUp() {
        NaturalFormatService formatService = new NaturalFormatService();
        cruiseFindService = new CruiseFindService(cruiseRepository, yachtRepository, formatService);
    }

    private Cruise createTestCruise(Long id, String status, Long startDate, Long startTime,
                                     Long endDate, Long endTime, String startHarbor,
                                     String destHarbor, Long yachtId,
                                     String p1w, String p2w, String p3w) {
        Cruise cruise = new Cruise();
        cruise.setCruiseId(id);
        cruise.setCruiseStatus(status);
        cruise.setStartDate(startDate);
        cruise.setStartTime(startTime);
        cruise.setEndDate(endDate);
        cruise.setEndTime(endTime);
        cruise.setStartHarbor(startHarbor);
        cruise.setDestinationHarbor(destHarbor);
        cruise.setIdYacht(yachtId);
        cruise.setPrice1w(new BigDecimal(p1w));
        cruise.setPrice2w(new BigDecimal(p2w));
        cruise.setPrice3w(new BigDecimal(p3w));
        return cruise;
    }

    private Yacht createTestYacht(Long id, String name) {
        Yacht yacht = new Yacht();
        yacht.setYachtId(id);
        yacht.setYachtName(name);
        yacht.setYachtType("Sailing Yacht");
        return yacht;
    }

    // --- Valid lookup tests ---

    @Test
    @DisplayName("Valid cruise lookup - all fields populated (cruise 671)")
    void testFindCruiseById_validCruise671() {
        // Setup: cruise 671 with status '0' (removed), yacht 1001 (Cassandra)
        Cruise cruise = createTestCruise(671L, "0", 20150801L, 100000L,
                20150820L, 70000L, "Samos", "Santorini", 1001L,
                "1000.000", "2000.000", "3000.000");
        Yacht yacht = createTestYacht(1001L, "Cassandra");

        when(cruiseRepository.findById(671L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1001L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(671L);

        assertNotNull(result);
        assertEquals(671L, result.getCruiseId());
        assertEquals("removed", result.getStatus());
        assertEquals("2015-08-01", result.getStartDate());
        assertEquals("100000 h", result.getStartTime());
        assertEquals("2015-08-20", result.getEndDate());
        assertEquals("70000 h", result.getEndTime());
        assertEquals("Samos", result.getFromHarbor());
        assertEquals("Santorini", result.getToHarbor());
        assertEquals("EUR  1000.00", result.getPrice1w());
        assertEquals("EUR  2000.00", result.getPrice2w());
        assertEquals("EUR  3000.00", result.getPrice3w());
        assertEquals("Cassandra", result.getYachtName());
    }

    @Test
    @DisplayName("Valid cruise lookup - status planned (cruise 675)")
    void testFindCruiseById_statusPlanned() {
        Cruise cruise = createTestCruise(675L, "1", 20160315L, 80000L,
                20160401L, 180000L, "Piraeus", "Mykonos", 1002L,
                "1500.500", "2800.000", "4000.000");
        Yacht yacht = createTestYacht(1002L, "Poseidon");

        when(cruiseRepository.findById(675L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1002L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(675L);

        assertEquals("planned", result.getStatus());
        assertEquals("2016-03-15", result.getStartDate());
        assertEquals("Poseidon", result.getYachtName());
        assertEquals("EUR  1500.50", result.getPrice1w());
    }

    @Test
    @DisplayName("Valid cruise lookup - status available (cruise 676)")
    void testFindCruiseById_statusAvailable() {
        Cruise cruise = createTestCruise(676L, "2", 20160601L, 90000L,
                20160615L, 170000L, "Rhodes", "Crete", 1003L,
                "2500.750", "4500.000", "6200.000");
        Yacht yacht = createTestYacht(1003L, "Athena");

        when(cruiseRepository.findById(676L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1003L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(676L);

        assertEquals("available", result.getStatus());
        assertEquals("Athena", result.getYachtName());
        assertEquals("EUR  2500.75", result.getPrice1w());
    }

    @Test
    @DisplayName("Valid cruise lookup - status sold (cruise 680)")
    void testFindCruiseById_statusSold() {
        Cruise cruise = createTestCruise(680L, "3", 20151231L, 120000L,
                20160115L, 150000L, "Corfu", "Dubrovnik", 1004L,
                "3500.000", "6500.000", "9000.000");
        Yacht yacht = createTestYacht(1004L, "Odysseus");

        when(cruiseRepository.findById(680L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1004L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(680L);

        assertEquals("sold", result.getStatus());
        assertEquals("2015-12-31", result.getStartDate());
        assertEquals("2016-01-15", result.getEndDate());
        assertEquals("Odysseus", result.getYachtName());
    }

    // --- Invalid lookup tests ---

    @Test
    @DisplayName("Cruise not found throws CruiseNotFoundException (RESET NC-PARMS + ESCAPE ROUTINE)")
    void testFindCruiseById_notFound() {
        when(cruiseRepository.findById(99999L)).thenReturn(Optional.empty());

        CruiseNotFoundException ex = assertThrows(CruiseNotFoundException.class,
                () -> cruiseFindService.findCruiseById(99999L));

        assertEquals(99999L, ex.getCruiseId());
        assertEquals("Sorry - No Cruise found for Id 99999", ex.getMessage());
    }

    // --- Unknown status code ---

    @Test
    @DisplayName("Unknown status code maps to 'unknown' (NONE case)")
    void testFindCruiseById_unknownStatus() {
        Cruise cruise = createTestCruise(690L, "9", 20170101L, 0L,
                20170201L, 235959L, "Athens", "Istanbul", 1005L,
                "5000.000", "9000.000", "12000.000");
        Yacht yacht = createTestYacht(1005L, "Artemis");

        when(cruiseRepository.findById(690L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1005L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(690L);

        assertEquals("unknown", result.getStatus());
    }

    // --- FK resolution edge cases ---

    @Test
    @DisplayName("Cruise with non-existent yacht returns empty yacht name")
    void testFindCruiseById_orphanYachtFK() {
        Cruise cruise = createTestCruise(692L, "1", 20160801L, 80000L,
                20160815L, 180000L, "Venice", "Split", 9999L,
                "1200.000", "2200.000", "3100.000");

        when(cruiseRepository.findById(692L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(9999L)).thenReturn(Optional.empty());

        CruiseDetailDto result = cruiseFindService.findCruiseById(692L);

        assertEquals("", result.getYachtName());
    }

    @Test
    @DisplayName("Cruise with null yacht ID returns empty yacht name")
    void testFindCruiseById_nullYachtId() {
        Cruise cruise = createTestCruise(693L, "2", 20160901L, 70000L,
                20160920L, 190000L, "Port A", "Port B", null,
                "1000.000", "2000.000", "3000.000");
        cruise.setIdYacht(null);

        when(cruiseRepository.findById(693L)).thenReturn(Optional.of(cruise));

        CruiseDetailDto result = cruiseFindService.findCruiseById(693L);

        assertEquals("", result.getYachtName());
    }

    // --- Zero price edge case ---

    @Test
    @DisplayName("Zero prices format correctly")
    void testFindCruiseById_zeroPrices() {
        Cruise cruise = createTestCruise(691L, "2", 20160701L, 60000L,
                20160715L, 200000L, "Naples", "Barcelona", 1001L,
                "0.000", "0.000", "0.000");
        Yacht yacht = createTestYacht(1001L, "Cassandra");

        when(cruiseRepository.findById(691L)).thenReturn(Optional.of(cruise));
        when(yachtRepository.findById(1001L)).thenReturn(Optional.of(yacht));

        CruiseDetailDto result = cruiseFindService.findCruiseById(691L);

        assertEquals("EUR     0.00", result.getPrice1w());
        assertEquals("EUR     0.00", result.getPrice2w());
        assertEquals("EUR     0.00", result.getPrice3w());
    }

    // --- Null harbor edge case ---

    @Test
    @DisplayName("Null harbors return empty strings")
    void testFindCruiseById_nullHarbors() {
        Cruise cruise = new Cruise();
        cruise.setCruiseId(694L);
        cruise.setCruiseStatus("2");
        cruise.setStartDate(20160801L);
        cruise.setStartTime(100000L);
        cruise.setEndDate(20160815L);
        cruise.setEndTime(180000L);
        cruise.setStartHarbor(null);
        cruise.setDestinationHarbor(null);
        cruise.setIdYacht(null);
        cruise.setPrice1w(new BigDecimal("1000.000"));
        cruise.setPrice2w(new BigDecimal("2000.000"));
        cruise.setPrice3w(new BigDecimal("3000.000"));

        when(cruiseRepository.findById(694L)).thenReturn(Optional.of(cruise));

        CruiseDetailDto result = cruiseFindService.findCruiseById(694L);

        assertEquals("", result.getFromHarbor());
        assertEquals("", result.getToHarbor());
    }
}
