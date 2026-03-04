package com.ntcruise.service;

import com.ntcruise.dto.CruiseFormDto;
import org.springframework.stereotype.Service;

/**
 * Service implementing NCWRFORP.NSP program logic.
 * Demonstrates WRITE USING FORM with hardcoded initial values.
 *
 * Original Natural source: Programs/NCWRFORP.NSP
 *
 * The program defines local variables with INIT values and writes
 * them using the NCDEFORM map (WRITE USING FORM 'NCDEFORM').
 *
 * This is a demo program that always returns the same hardcoded data.
 */
@Service
public class CruiseFormService {

    /**
     * Returns the hardcoded form data from NCWRFORP.NSP.
     *
     * Maps NCWRFORP.NSP (lines 14-27):
     *   #CR-ED          INIT <'2015-08-01'>
     *   #CR-ET          INIT <'7 h'>
     *   #CR-FROMH       INIT <'Samos'>
     *   #CR-ID          INIT <12345678>
     *   #CR-P1W         INIT <'1000.00'>
     *   #CR-P2W         INIT <'2000.00'>
     *   #CR-P3W         INIT <'3000.00'>
     *   #CR-SD          INIT <'2015-08-20'>
     *   #CR-ST          INIT <'10 h'>
     *   #CR-STATUS      INIT <'available'>
     *   #CR-TOH         INIT <'Santorini'>
     *   #CR-YACHT-NAME  INIT <'Cassandra'>
     *
     * @return CruiseFormDto with hardcoded values matching Natural INIT values
     */
    public CruiseFormDto getFormData() {
        CruiseFormDto dto = new CruiseFormDto();

        // NCWRFORP.NSP line 18: #CR-ID (N08.0) INIT <12345678>
        dto.setCruiseId(12345678L);

        // NCWRFORP.NSP line 24: #CR-STATUS (A020) INIT <'available'>
        dto.setStatus("available");

        // NCWRFORP.NSP line 22: #CR-SD (A013) INIT <'2015-08-20'>
        dto.setStartDate("2015-08-20");

        // NCWRFORP.NSP line 23: #CR-ST (A007) INIT <'10 h'>
        dto.setStartTime("10 h");

        // NCWRFORP.NSP line 15: #CR-ED (A013) INIT <'2015-08-01'>
        dto.setEndDate("2015-08-01");

        // NCWRFORP.NSP line 16: #CR-ET (A007) INIT <'7 h'>
        dto.setEndTime("7 h");

        // NCWRFORP.NSP line 17: #CR-FROMH (A020) INIT <'Samos'>
        dto.setFromHarbor("Samos");

        // NCWRFORP.NSP line 25: #CR-TOH (A020) INIT <'Santorini'>
        dto.setToHarbor("Santorini");

        // NCWRFORP.NSP line 19: #CR-P1W (A020) INIT <'1000.00'>
        dto.setPrice1w("1000.00");

        // NCWRFORP.NSP line 20: #CR-P2W (A020) INIT <'2000.00'>
        dto.setPrice2w("2000.00");

        // NCWRFORP.NSP line 21: #CR-P3W (A020) INIT <'3000.00'>
        dto.setPrice3w("3000.00");

        // NCWRFORP.NSP line 26: #CR-YACHT-NAME (A020) INIT <'Cassandra'>
        dto.setYachtName("Cassandra");

        return dto;
    }
}
