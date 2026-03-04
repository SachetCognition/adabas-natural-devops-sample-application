package com.ntcruise.service;

import com.ntcruise.dto.CruiseFormDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for CruiseFormService.
 * Validates hardcoded form data matches NCWRFORP.NSP INIT values exactly.
 */
class CruiseFormServiceTest {

    private CruiseFormService formService;

    @BeforeEach
    void setUp() {
        formService = new CruiseFormService();
    }

    @Test
    @DisplayName("Form data matches NCWRFORP.NSP INIT values exactly")
    void testGetFormData() {
        CruiseFormDto dto = formService.getFormData();

        assertNotNull(dto);

        // NCWRFORP.NSP line 18: #CR-ID (N08.0) INIT <12345678>
        assertEquals(12345678L, dto.getCruiseId());

        // NCWRFORP.NSP line 24: #CR-STATUS (A020) INIT <'available'>
        assertEquals("available", dto.getStatus());

        // NCWRFORP.NSP line 22: #CR-SD (A013) INIT <'2015-08-20'>
        assertEquals("2015-08-20", dto.getStartDate());

        // NCWRFORP.NSP line 23: #CR-ST (A007) INIT <'10 h'>
        assertEquals("10 h", dto.getStartTime());

        // NCWRFORP.NSP line 15: #CR-ED (A013) INIT <'2015-08-01'>
        assertEquals("2015-08-01", dto.getEndDate());

        // NCWRFORP.NSP line 16: #CR-ET (A007) INIT <'7 h'>
        assertEquals("7 h", dto.getEndTime());

        // NCWRFORP.NSP line 17: #CR-FROMH (A020) INIT <'Samos'>
        assertEquals("Samos", dto.getFromHarbor());

        // NCWRFORP.NSP line 25: #CR-TOH (A020) INIT <'Santorini'>
        assertEquals("Santorini", dto.getToHarbor());

        // NCWRFORP.NSP line 19: #CR-P1W (A020) INIT <'1000.00'>
        assertEquals("1000.00", dto.getPrice1w());

        // NCWRFORP.NSP line 20: #CR-P2W (A020) INIT <'2000.00'>
        assertEquals("2000.00", dto.getPrice2w());

        // NCWRFORP.NSP line 21: #CR-P3W (A020) INIT <'3000.00'>
        assertEquals("3000.00", dto.getPrice3w());

        // NCWRFORP.NSP line 26: #CR-YACHT-NAME (A020) INIT <'Cassandra'>
        assertEquals("Cassandra", dto.getYachtName());
    }
}
