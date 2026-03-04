package com.ntcruise.controller;

import com.ntcruise.dto.CruiseDetailDto;
import com.ntcruise.dto.CruiseDisplayItemDto;
import com.ntcruise.dto.CruiseFormDto;
import com.ntcruise.dto.CruiseListItemDto;
import com.ntcruise.dto.SystemVariablesDto;
import com.ntcruise.service.CruiseFindService;
import com.ntcruise.service.CruiseFormService;
import com.ntcruise.service.CruiseReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller mapping Natural interactive programs to REST endpoints.
 *
 * Endpoint mapping:
 *   NCINMAPP.NSP (cruise lookup)     -> GET /api/cruises/{id}
 *   NCATENDP.NSP (report w/ paging)  -> GET /api/cruises?page=0&size=40
 *   NCATTOPP.NSP (report w/ paging)  -> GET /api/cruises/top-page?page=0&size=40
 *   NCDEDISP.NSP (display w/ masks)  -> GET /api/cruises/display?page=0&size=100
 *   NCSYSVP.NSP  (system vars demo)  -> GET /api/cruises/system-variables?page=0&size=10
 *   NCWRFORP.NSP (form demo)         -> GET /api/cruises/form
 */
@RestController
@RequestMapping("/api/cruises")
public class CruiseController {

    private final CruiseFindService cruiseFindService;
    private final CruiseReportService cruiseReportService;
    private final CruiseFormService cruiseFormService;

    public CruiseController(CruiseFindService cruiseFindService,
                            CruiseReportService cruiseReportService,
                            CruiseFormService cruiseFormService) {
        this.cruiseFindService = cruiseFindService;
        this.cruiseReportService = cruiseReportService;
        this.cruiseFormService = cruiseFormService;
    }

    /**
     * Cruise lookup by ID.
     * Maps NCINMAPP.NSP -> CALLNAT 'NCFINDCR' NC-PARMS (line 80).
     *
     * @param id the cruise ID (maps to #CR-ID-FIND)
     * @return cruise detail with all formatted fields
     */
    @GetMapping("/{id}")
    public ResponseEntity<CruiseDetailDto> getCruiseById(@PathVariable("id") Long id) {
        CruiseDetailDto dto = cruiseFindService.findCruiseById(id);
        return ResponseEntity.ok(dto);
    }

    /**
     * Paginated cruise report with AT END OF PAGE.
     * Maps NCATENDP.NSP: READ (40) NCCRUISE with yacht join.
     *
     * @param page page number (default 0)
     * @param size page size (default 40, matching Natural READ (40))
     * @return paginated cruise list items
     */
    @GetMapping
    public ResponseEntity<Page<CruiseListItemDto>> getCruiseReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Paginated cruise report with AT TOP OF PAGE.
     * Maps NCATTOPP.NSP: READ (40) NCCRUISE - same data as getCruiseReport
     * but represents the AT TOP OF PAGE variant.
     *
     * @param page page number (default 0)
     * @param size page size (default 40)
     * @return paginated cruise list items
     */
    @GetMapping("/top-page")
    public ResponseEntity<Page<CruiseListItemDto>> getCruiseTopPageReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        Page<CruiseListItemDto> result = cruiseReportService.getCruiseReport(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Display report with edit masks.
     * Maps NCDEDISP.NSP: READ (100) NCCRUISE with DISPLAY and edit masks.
     *
     * @param page page number (default 0)
     * @param size page size (default 100, matching Natural READ (100))
     * @return paginated display items with price formatting
     */
    @GetMapping("/display")
    public ResponseEntity<Page<CruiseDisplayItemDto>> getCruiseDisplay(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Page<CruiseDisplayItemDto> result = cruiseReportService.getCruiseDisplay(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * System variables report.
     * Maps NCSYSVP.NSP: READ (10) NCCRUISE with truncated yacht name.
     *
     * @param page page number (default 0)
     * @param size page size (default 10, matching Natural READ (10))
     * @return paginated system variable items
     */
    @GetMapping("/system-variables")
    public ResponseEntity<Page<SystemVariablesDto>> getSystemVariablesReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SystemVariablesDto> result = cruiseReportService.getSystemVariablesReport(page, size);
        return ResponseEntity.ok(result);
    }

    /**
     * Form output demo.
     * Maps NCWRFORP.NSP: WRITE USING FORM 'NCDEFORM' with hardcoded values.
     *
     * @return form DTO with hardcoded cruise data
     */
    @GetMapping("/form")
    public ResponseEntity<CruiseFormDto> getCruiseForm() {
        CruiseFormDto dto = cruiseFormService.getFormData();
        return ResponseEntity.ok(dto);
    }
}
