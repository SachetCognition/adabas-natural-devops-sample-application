package com.ntcruise.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for CruiseController.
 * Tests the full request-to-database stack using MockMvc with H2 in-memory database.
 * Flyway migrations (V1-V3) run automatically to create schema and load sample data.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CruiseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // --- GET /api/cruises/{id} - Valid resource retrieval ---

    @Test
    @DisplayName("GET /api/cruises/671 returns 200 with correct cruise detail")
    void testGetCruiseById_200() throws Exception {
        mockMvc.perform(get("/api/cruises/671")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cruiseId", is(671)))
                .andExpect(jsonPath("$.status", is("removed")))
                .andExpect(jsonPath("$.startDate", is("2015-08-01")))
                .andExpect(jsonPath("$.startTime", is("100000 h")))
                .andExpect(jsonPath("$.endDate", is("2015-08-20")))
                .andExpect(jsonPath("$.endTime", is("70000 h")))
                .andExpect(jsonPath("$.fromHarbor", is("Samos")))
                .andExpect(jsonPath("$.toHarbor", is("Santorini")))
                .andExpect(jsonPath("$.price1w", is("EUR  1000.00")))
                .andExpect(jsonPath("$.price2w", is("EUR  2000.00")))
                .andExpect(jsonPath("$.price3w", is("EUR  3000.00")))
                .andExpect(jsonPath("$.yachtName", is("Cassandra")));
    }

    @Test
    @DisplayName("GET /api/cruises/675 returns planned status")
    void testGetCruiseById_planned() throws Exception {
        mockMvc.perform(get("/api/cruises/675")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("planned")))
                .andExpect(jsonPath("$.yachtName", is("Poseidon")));
    }

    @Test
    @DisplayName("GET /api/cruises/676 returns available status")
    void testGetCruiseById_available() throws Exception {
        mockMvc.perform(get("/api/cruises/676")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("available")))
                .andExpect(jsonPath("$.yachtName", is("Athena")));
    }

    @Test
    @DisplayName("GET /api/cruises/680 returns sold status with year-end boundary dates")
    void testGetCruiseById_sold() throws Exception {
        mockMvc.perform(get("/api/cruises/680")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("sold")))
                .andExpect(jsonPath("$.startDate", is("2015-12-31")))
                .andExpect(jsonPath("$.endDate", is("2016-01-15")));
    }

    // --- GET /api/cruises/{id} - Missing resource ---

    @Test
    @DisplayName("GET /api/cruises/99999 returns 404 with structured error body")
    void testGetCruiseById_404() throws Exception {
        mockMvc.perform(get("/api/cruises/99999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error", is("Not Found")))
                .andExpect(jsonPath("$.message", is("Sorry - No Cruise found for Id 99999")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    // --- GET /api/cruises - Paginated list ---

    @Test
    @DisplayName("GET /api/cruises returns paginated cruise list")
    void testGetCruiseReport_paged() throws Exception {
        mockMvc.perform(get("/api/cruises")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.size", is(5)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    @Test
    @DisplayName("GET /api/cruises with default size returns up to 40 records")
    void testGetCruiseReport_defaultSize() throws Exception {
        mockMvc.perform(get("/api/cruises")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(40)))
                .andExpect(jsonPath("$.number", is(0)));
    }

    // --- GET /api/cruises/display ---

    @Test
    @DisplayName("GET /api/cruises/display returns display items with prices")
    void testGetCruiseDisplay() throws Exception {
        mockMvc.perform(get("/api/cruises/display")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].price1w", notNullValue()))
                .andExpect(jsonPath("$.content[0].yachtName", notNullValue()))
                .andExpect(jsonPath("$.content[0].startDate", notNullValue()));
    }

    // --- GET /api/cruises/system-variables ---

    @Test
    @DisplayName("GET /api/cruises/system-variables returns system variable items")
    void testGetSystemVariablesReport() throws Exception {
        mockMvc.perform(get("/api/cruises/system-variables")
                        .param("page", "0")
                        .param("size", "10")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    // --- GET /api/cruises/form ---

    @Test
    @DisplayName("GET /api/cruises/form returns hardcoded form data matching NCWRFORP INIT values")
    void testGetCruiseForm() throws Exception {
        mockMvc.perform(get("/api/cruises/form")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cruiseId", is(12345678)))
                .andExpect(jsonPath("$.status", is("available")))
                .andExpect(jsonPath("$.startDate", is("2015-08-20")))
                .andExpect(jsonPath("$.startTime", is("10 h")))
                .andExpect(jsonPath("$.endDate", is("2015-08-01")))
                .andExpect(jsonPath("$.endTime", is("7 h")))
                .andExpect(jsonPath("$.fromHarbor", is("Samos")))
                .andExpect(jsonPath("$.toHarbor", is("Santorini")))
                .andExpect(jsonPath("$.price1w", is("1000.00")))
                .andExpect(jsonPath("$.price2w", is("2000.00")))
                .andExpect(jsonPath("$.price3w", is("3000.00")))
                .andExpect(jsonPath("$.yachtName", is("Cassandra")));
    }

    // --- GET /api/cruises/top-page ---

    @Test
    @DisplayName("GET /api/cruises/top-page returns same structure as cruise report")
    void testGetCruiseTopPageReport() throws Exception {
        mockMvc.perform(get("/api/cruises/top-page")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.size", is(5)));
    }

    // --- Edge cases ---

    @Test
    @DisplayName("GET /api/cruises/690 returns unknown status for invalid status code")
    void testGetCruiseById_unknownStatus() throws Exception {
        mockMvc.perform(get("/api/cruises/690")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("unknown")));
    }

    @Test
    @DisplayName("GET /api/cruises/692 returns empty yacht name for orphan FK")
    void testGetCruiseById_orphanYachtFK() throws Exception {
        mockMvc.perform(get("/api/cruises/692")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yachtName", is("")));
    }

    @Test
    @DisplayName("GET /api/cruises/691 returns zero prices formatted correctly")
    void testGetCruiseById_zeroPrices() throws Exception {
        mockMvc.perform(get("/api/cruises/691")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price1w", is("EUR     0.00")))
                .andExpect(jsonPath("$.price2w", is("EUR     0.00")))
                .andExpect(jsonPath("$.price3w", is("EUR     0.00")));
    }
}
