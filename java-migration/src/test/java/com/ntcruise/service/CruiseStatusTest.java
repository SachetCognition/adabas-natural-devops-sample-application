package com.ntcruise.service;

import com.ntcruise.model.CruiseStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for CruiseStatus enum.
 * Validates every DECIDE ON FIRST VALUE branch from NCFINDCR.NSN (lines 36-43).
 *
 * Original Natural code:
 *   DECIDE ON FIRST VALUE OF #CR-STATUS
 *     VALUE '0'  MOVE 'removed'    TO #CR-STATUS
 *     VALUE '1'  MOVE 'planned'    TO #CR-STATUS
 *     VALUE '2'  MOVE 'available'  TO #CR-STATUS
 *     VALUE '3'  MOVE 'sold'       TO #CR-STATUS
 *     NONE       MOVE 'unknown'    TO #CR-STATUS
 *   END-DECIDE
 */
class CruiseStatusTest {

    @ParameterizedTest(name = "Status code ''{0}'' maps to ''{1}''")
    @CsvSource({
        "0, removed",
        "1, planned",
        "2, available",
        "3, sold",
        "4, unknown",
        "5, unknown",
        "9, unknown",
        "X, unknown",
        "'', unknown"
    })
    @DisplayName("DECIDE ON FIRST VALUE status mapping")
    void testStatusMapping(String code, String expectedDisplay) {
        assertEquals(expectedDisplay, CruiseStatus.toDisplayName(code));
    }

    @ParameterizedTest(name = "fromCode(''{0}'') returns correct enum")
    @CsvSource({
        "0, REMOVED",
        "1, PLANNED",
        "2, AVAILABLE",
        "3, SOLD"
    })
    @DisplayName("fromCode returns correct enum value")
    void testFromCode(String code, String expectedEnum) {
        CruiseStatus result = CruiseStatus.fromCode(code);
        assertEquals(CruiseStatus.valueOf(expectedEnum), result);
    }
}
