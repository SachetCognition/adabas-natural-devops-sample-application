package com.ntcruise.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for NaturalFormatService.
 * Validates that Java formatting matches Natural edit masks exactly.
 */
class NaturalFormatServiceTest {

    private NaturalFormatService formatService;

    @BeforeEach
    void setUp() {
        formatService = new NaturalFormatService();
    }

    // --- Date formatting: EM=9999'-'99'-'99 ---

    @ParameterizedTest(name = "formatDate({0}) = \"{1}\"")
    @CsvSource({
        "20150801, 2015-08-01",
        "20150820, 2015-08-20",
        "20160315, 2016-03-15",
        "20160601, 2016-06-01",
        "20151231, 2015-12-31",
        "20160115, 2016-01-15",
        "20170101, 2017-01-01",
        "20170201, 2017-02-01"
    })
    @DisplayName("Date formatting matches EM=9999'-'99'-'99")
    void testFormatDate(long input, String expected) {
        assertEquals(expected, formatService.formatDate(input));
    }

    @Test
    @DisplayName("Null date returns empty string")
    void testFormatDateNull() {
        assertEquals("", formatService.formatDate(null));
    }

    @Test
    @DisplayName("Zero date returns empty string")
    void testFormatDateZero() {
        assertEquals("", formatService.formatDate(0L));
    }

    // --- Time formatting: COMPRESS <time> 'h' ---

    @ParameterizedTest(name = "formatTime({0}) = \"{1}\"")
    @CsvSource({
        "100000, 100000 h",
        "70000, 70000 h",
        "180000, 180000 h",
        "80000, 80000 h",
        "120000, 120000 h",
        "0, 0 h",
        "235959, 235959 h"
    })
    @DisplayName("Time formatting matches COMPRESS <time> 'h'")
    void testFormatTime(long input, String expected) {
        assertEquals(expected, formatService.formatTime(input));
    }

    @Test
    @DisplayName("Null time returns empty string")
    void testFormatTimeNull() {
        assertEquals("", formatService.formatTime(null));
    }

    // --- Price formatting: EM=*EUR' 'ZZZZ9.99 ---

    @Test
    @DisplayName("Price 1000.000 formats as EUR  1000.00")
    void testFormatPrice1000() {
        BigDecimal price = new BigDecimal("1000.000");
        String result = formatService.formatPrice(price);
        assertEquals("EUR  1000.00", result);
    }

    @Test
    @DisplayName("Price 2000.000 formats correctly")
    void testFormatPrice2000() {
        BigDecimal price = new BigDecimal("2000.000");
        String result = formatService.formatPrice(price);
        assertEquals("EUR  2000.00", result);
    }

    @Test
    @DisplayName("Price 3000.000 formats correctly")
    void testFormatPrice3000() {
        BigDecimal price = new BigDecimal("3000.000");
        String result = formatService.formatPrice(price);
        assertEquals("EUR  3000.00", result);
    }

    @Test
    @DisplayName("Price with decimals formats correctly")
    void testFormatPriceWithDecimals() {
        BigDecimal price = new BigDecimal("1500.500");
        String result = formatService.formatPrice(price);
        assertEquals("EUR  1500.50", result);
    }

    @Test
    @DisplayName("Zero price formats as EUR     0.00")
    void testFormatPriceZero() {
        BigDecimal price = BigDecimal.ZERO;
        String result = formatService.formatPrice(price);
        assertEquals("EUR     0.00", result);
    }

    @Test
    @DisplayName("Large price formats correctly")
    void testFormatPriceLarge() {
        BigDecimal price = new BigDecimal("12000.000");
        String result = formatService.formatPrice(price);
        assertEquals("EUR 12000.00", result);
    }

    @Test
    @DisplayName("Null price returns empty string")
    void testFormatPriceNull() {
        assertEquals("", formatService.formatPrice(null));
    }

    @Test
    @DisplayName("Price with fractional cents rounds correctly")
    void testFormatPriceRounding() {
        BigDecimal price = new BigDecimal("2500.750");
        String result = formatService.formatPrice(price);
        assertEquals("EUR  2500.75", result);
    }
}
