package com.ntcruise.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Service for Natural edit mask formatting.
 * Provides exact equivalents for Natural MOVE EDITED operations.
 *
 * Natural edit masks used in NCFINDCR.NSN:
 *   EM=9999'-'99'-'99        -> date formatting YYYY-MM-DD
 *   EM=*EUR' 'ZZZZ9.99      -> EUR currency formatting with leading zero suppression
 *
 * And COMPRESS with suffix:
 *   COMPRESS <time> 'h' INTO <target> -> time formatting with " h" suffix
 */
@Service
public class NaturalFormatService {

    /**
     * Formats an Adabas N8.0 date (YYYYMMDD) using Natural edit mask EM=9999'-'99'-'99.
     * Produces exactly YYYY-MM-DD format.
     *
     * Source: NCFINDCR.NSN line 23:
     *   MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
     *
     * @param dateValue the N8.0 date value (e.g., 20150801)
     * @return formatted date string (e.g., "2015-08-01"), or empty string if null/zero
     */
    public String formatDate(Long dateValue) {
        if (dateValue == null || dateValue == 0) {
            return "";
        }
        // EM=9999'-'99'-'99 means: 4 digits, dash, 2 digits, dash, 2 digits
        String raw = String.format("%08d", dateValue);
        return raw.substring(0, 4) + "-" + raw.substring(4, 6) + "-" + raw.substring(6, 8);
    }

    /**
     * Formats an Adabas N6.0 time using Natural COMPRESS with 'h' suffix.
     * Produces "NNNNNN h" format (raw time value followed by space and 'h').
     *
     * Source: NCFINDCR.NSN line 24:
     *   COMPRESS NCCRUISE.START-TIME 'h' INTO #CR-ST
     *
     * Natural COMPRESS concatenates values with a single space separator by default.
     *
     * @param timeValue the N6.0 time value (e.g., 100000 for 10:00:00)
     * @return formatted time string (e.g., "100000 h"), or empty string if null
     */
    public String formatTime(Long timeValue) {
        if (timeValue == null) {
            return "";
        }
        // COMPRESS <value> 'h' places a space between the value and 'h'
        // Natural numeric display suppresses leading zeros
        return timeValue + " h";
    }

    /**
     * Formats a price using Natural edit mask EM=*EUR' 'ZZZZ9.99.
     * Produces "EUR  NNNN9.99" with leading zero suppression (Z = suppress leading zeros).
     *
     * The * at the start means use the currency symbol EUR.
     * ZZZZ means suppress leading zeros for 4 digit positions.
     * 9 means always display this digit.
     * .99 means 2 decimal places.
     *
     * Source: NCFINDCR.NSN lines 31-33:
     *   MOVE EDITED NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99) TO #CR-P1W
     *
     * @param price the packed decimal price value
     * @return formatted price string (e.g., "EUR  1000.00"), or empty string if null
     */
    public String formatPrice(BigDecimal price) {
        if (price == null) {
            return "";
        }
        // EM=*EUR' 'ZZZZ9.99
        // EUR prefix, space, then the number with leading zero suppression
        // ZZZZ9.99 = 5 integer positions (4 suppressible + 1 mandatory), 2 decimal
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator('.');
        // ZZZZ9 = leading zeros suppressed, minimum 1 digit before decimal
        // .99 = exactly 2 decimal places
        DecimalFormat format = new DecimalFormat("#####0.00", symbols);
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);

        String formatted = format.format(price.setScale(2, java.math.RoundingMode.HALF_UP));

        // Pad to match Natural's fixed-width output: ZZZZ9 = 5 positions + .99 = 2
        // Total numeric part: up to 8 chars (e.g., "12345.67" or "    0.00")
        // But Natural ZZZZ9.99 with leading spaces for suppressed zeros
        String numericPart = String.format("%8s", formatted);

        return "EUR " + numericPart;
    }
}
