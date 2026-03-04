package com.ntcruise.service;

import com.ntcruise.dto.CruiseDisplayItemDto;
import com.ntcruise.dto.CruiseListItemDto;
import com.ntcruise.dto.SystemVariablesDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CruiseReportService.
 * Validates report generation matching NCATENDP, NCDEDISP, NCSYSVP programs.
 */
@ExtendWith(MockitoExtension.class)
class CruiseReportServiceTest {

    @Mock
    private CruiseRepository cruiseRepository;

    @Mock
    private YachtRepository yachtRepository;

    private CruiseReportService cruiseReportService;

    @BeforeEach
    void setUp() {
        NaturalFormatService formatService = new NaturalFormatService();
        cruiseReportService = new CruiseReportService(cruiseRepository, yachtRepository, formatService);
    }

    private Cruise createCruise(Long id, Long startDate, Long endDate,
                                 String startHarbor, String destHarbor, Long yachtId, String price1w) {
        Cruise cruise = new Cruise();
        cruise.setCruiseId(id);
        cruise.setCruiseStatus("2");
        cruise.setStartDate(startDate);
        cruise.setStartTime(100000L);
        cruise.setEndDate(endDate);
        cruise.setEndTime(180000L);
        cruise.setStartHarbor(startHarbor);
        cruise.setDestinationHarbor(destHarbor);
        cruise.setIdYacht(yachtId);
        cruise.setPrice1w(new BigDecimal(price1w));
        cruise.setPrice2w(new BigDecimal("2000.000"));
        cruise.setPrice3w(new BigDecimal("3000.000"));
        return cruise;
    }

    // --- NCATENDP report tests ---

    @Test
    @DisplayName("Cruise report returns paginated results with formatted dates")
    void testGetCruiseReport() {
        Cruise cruise = createCruise(671L, 20150801L, 20150820L, "Samos", "Santorini", 1001L, "1000.000");
        Yacht yacht = new Yacht();
        yacht.setYachtId(1001L);
        yacht.setYachtName("Cassandra");

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 40), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(yachtRepository.findById(1001L)).thenReturn(Optional.of(yacht));

        Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(0, 40);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        CruiseListItemDto item = result.getContent().get(0);
        assertEquals("Cassandra", item.getYachtName());
        assertEquals("2015-08-01", item.getStartDate());
        assertEquals("2015-08-20", item.getEndDate());
        assertEquals("Samos", item.getStartHarbor());
        assertEquals("Santorini", item.getDestinationHarbor());
    }

    @Test
    @DisplayName("Cruise report with missing yacht returns empty yacht name")
    void testGetCruiseReport_missingYacht() {
        Cruise cruise = createCruise(692L, 20160801L, 20160815L, "Venice", "Split", 9999L, "1200.000");

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 40), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(yachtRepository.findById(9999L)).thenReturn(Optional.empty());

        Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(0, 40);

        assertEquals("", result.getContent().get(0).getYachtName());
    }

    @Test
    @DisplayName("Cruise report with null harbors returns empty strings")
    void testGetCruiseReport_nullHarbors() {
        Cruise cruise = createCruise(693L, 20160801L, 20160815L, null, null, null, "1000.000");

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 40), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(0, 40);

        CruiseListItemDto item = result.getContent().get(0);
        assertEquals("", item.getStartHarbor());
        assertEquals("", item.getDestinationHarbor());
        assertEquals("", item.getYachtName());
    }

    // --- NCDEDISP display tests ---

    @Test
    @DisplayName("Cruise display includes formatted price with EUR")
    void testGetCruiseDisplay() {
        Cruise cruise = createCruise(671L, 20150801L, 20150820L, "Samos", "Santorini", 1001L, "1000.000");
        Yacht yacht = new Yacht();
        yacht.setYachtId(1001L);
        yacht.setYachtName("Cassandra");

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 100), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(yachtRepository.findById(1001L)).thenReturn(Optional.of(yacht));

        Page<CruiseDisplayItemDto> result = cruiseReportService.getCruiseDisplay(0, 100);

        assertNotNull(result);
        CruiseDisplayItemDto item = result.getContent().get(0);
        assertEquals("Cassandra", item.getYachtName());
        assertEquals("EUR  1000.00", item.getPrice1w());
        assertEquals("2015-08-01", item.getStartDate());
    }

    // --- NCSYSVP system variables tests ---

    @Test
    @DisplayName("System variables report truncates yacht name to 10 chars")
    void testGetSystemVariablesReport_truncation() {
        Cruise cruise = createCruise(671L, 20150801L, 20150820L, "Samos", "Santorini", 1001L, "1000.000");
        Yacht yacht = new Yacht();
        yacht.setYachtId(1001L);
        yacht.setYachtName("Cassandra Long Name");  // > 10 chars

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 10), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(yachtRepository.findById(1001L)).thenReturn(Optional.of(yacht));

        Page<SystemVariablesDto> result = cruiseReportService.getSystemVariablesReport(0, 10);

        assertNotNull(result);
        SystemVariablesDto item = result.getContent().get(0);
        // NCSYSVP.NSP: #YACHT-NAME (A10) - truncated to 10 chars
        assertEquals("Cassandra ", item.getYachtName());
    }

    @Test
    @DisplayName("System variables report preserves short yacht names")
    void testGetSystemVariablesReport_shortName() {
        Cruise cruise = createCruise(676L, 20160601L, 20160615L, "Rhodes", "Crete", 1003L, "2500.750");
        Yacht yacht = new Yacht();
        yacht.setYachtId(1003L);
        yacht.setYachtName("Athena");  // < 10 chars

        Page<Cruise> page = new PageImpl<>(List.of(cruise), PageRequest.of(0, 10), 1);
        when(cruiseRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(yachtRepository.findById(1003L)).thenReturn(Optional.of(yacht));

        Page<SystemVariablesDto> result = cruiseReportService.getSystemVariablesReport(0, 10);

        SystemVariablesDto item = result.getContent().get(0);
        assertEquals("Athena", item.getYachtName());
    }
}
