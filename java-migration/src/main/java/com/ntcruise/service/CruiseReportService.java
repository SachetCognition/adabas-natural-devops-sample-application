package com.ntcruise.service;

import com.ntcruise.dto.CruiseDisplayItemDto;
import com.ntcruise.dto.CruiseListItemDto;
import com.ntcruise.dto.SystemVariablesDto;
import com.ntcruise.model.Cruise;
import com.ntcruise.model.Yacht;
import com.ntcruise.repository.CruiseRepository;
import com.ntcruise.repository.YachtRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service implementing report program logic from NCATENDP, NCATTOPP, NCDEDISP, NCSYSVP.
 *
 * These programs all follow the same pattern:
 * READ (N) NCCRUISE -> for each cruise, FIND NCYACHT by ID-YACHT -> format and output
 *
 * Original Natural sources:
 * - NCATENDP.NSP: AT END OF PAGE report with yacht name, dates, harbors (READ 40)
 * - NCATTOPP.NSP: AT TOP OF PAGE report same fields (READ 40)
 * - NCDEDISP.NSP: DISPLAY with edit masks including price (READ 100)
 * - NCSYSVP.NSP: System variables demo with truncated yacht name (READ 10)
 */
@Service
public class CruiseReportService {

    private final CruiseRepository cruiseRepository;
    private final YachtRepository yachtRepository;
    private final NaturalFormatService formatService;

    public CruiseReportService(CruiseRepository cruiseRepository,
                               YachtRepository yachtRepository,
                               NaturalFormatService formatService) {
        this.cruiseRepository = cruiseRepository;
        this.yachtRepository = yachtRepository;
        this.formatService = formatService;
    }

    /**
     * Paginated cruise list for NCATENDP/NCATTOPP report programs.
     *
     * Maps NCATENDP.NSP (lines 36-48):
     *   R1. READ (40) NCCRUISE
     *     F1. FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
     *       WRITE NOHDR
     *         NCYACHT.YACHT-NAME (AL=15)
     *         NCCRUISE.START-DATE (EM=9999'-'99'-'99)
     *         NCCRUISE.END-DATE (EM=9999'-'99'-'99)
     *         NCCRUISE.START-HARBOR (AL=15)
     *         NCCRUISE.DESTINATION-HARBOR (AL=15)
     *
     * @param page page number (0-based)
     * @param size page size (default 40 matching Natural READ (40))
     * @return paginated list of cruise items
     */
    @Transactional(readOnly = true)
    public Page<CruiseListItemDto> getCruiseReport(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cruise> cruisePage = cruiseRepository.findAll(pageable);

        List<CruiseListItemDto> items = new ArrayList<>();
        for (Cruise cruise : cruisePage.getContent()) {
            CruiseListItemDto item = new CruiseListItemDto();
            item.setCruiseId(cruise.getCruiseId());

            // NCATENDP.NSP line 42: NCCRUISE.START-DATE (EM=9999'-'99'-'99)
            item.setStartDate(formatService.formatDate(cruise.getStartDate()));

            // NCATENDP.NSP line 43: NCCRUISE.END-DATE (EM=9999'-'99'-'99)
            item.setEndDate(formatService.formatDate(cruise.getEndDate()));

            // NCATENDP.NSP line 44: NCCRUISE.START-HARBOR (AL=15)
            item.setStartHarbor(cruise.getStartHarbor() != null ? cruise.getStartHarbor() : "");

            // NCATENDP.NSP line 45: NCCRUISE.DESTINATION-HARBOR (AL=15)
            item.setDestinationHarbor(cruise.getDestinationHarbor() != null ? cruise.getDestinationHarbor() : "");

            // NCATENDP.NSP line 37: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
            // NCATENDP.NSP line 41: NCYACHT.YACHT-NAME (AL=15)
            String yachtName = resolveYachtName(cruise.getIdYacht());
            item.setYachtName(yachtName);

            items.add(item);
        }

        return new PageImpl<>(items, pageable, cruisePage.getTotalElements());
    }

    /**
     * Display report with edit masks for NCDEDISP program.
     *
     * Maps NCDEDISP.NSP (lines 19-31):
     *   READ (100) NCCRUISE
     *     FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
     *       DISPLAY
     *         NCYACHT.YACHT-NAME
     *         NCCRUISE.START-DATE (EM=9999'-'99'-'99)
     *         NCCRUISE.START-HARBOR (AL=10)
     *         NCCRUISE.END-DATE (EM=9999'-'99'-'99)
     *         NCCRUISE.DESTINATION-HARBOR (AL=10)
     *         NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99)
     *
     * @param page page number (0-based)
     * @param size page size (default 100 matching Natural READ (100))
     * @return paginated list of display items with price
     */
    @Transactional(readOnly = true)
    public Page<CruiseDisplayItemDto> getCruiseDisplay(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cruise> cruisePage = cruiseRepository.findAll(pageable);

        List<CruiseDisplayItemDto> items = new ArrayList<>();
        for (Cruise cruise : cruisePage.getContent()) {
            CruiseDisplayItemDto item = new CruiseDisplayItemDto();
            item.setCruiseId(cruise.getCruiseId());

            // NCDEDISP.NSP line 24: NCCRUISE.START-DATE (EM=9999'-'99'-'99)
            item.setStartDate(formatService.formatDate(cruise.getStartDate()));

            // NCDEDISP.NSP line 25: NCCRUISE.START-HARBOR (AL=10)
            item.setStartHarbor(cruise.getStartHarbor() != null ? cruise.getStartHarbor() : "");

            // NCDEDISP.NSP line 26: NCCRUISE.END-DATE (EM=9999'-'99'-'99)
            item.setEndDate(formatService.formatDate(cruise.getEndDate()));

            // NCDEDISP.NSP line 27: NCCRUISE.DESTINATION-HARBOR (AL=10)
            item.setDestinationHarbor(cruise.getDestinationHarbor() != null ? cruise.getDestinationHarbor() : "");

            // NCDEDISP.NSP line 28: NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99)
            item.setPrice1w(formatService.formatPrice(cruise.getPrice1w()));

            // NCDEDISP.NSP line 20: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
            // NCDEDISP.NSP line 23: NCYACHT.YACHT-NAME
            item.setYachtName(resolveYachtName(cruise.getIdYacht()));

            items.add(item);
        }

        return new PageImpl<>(items, pageable, cruisePage.getTotalElements());
    }

    /**
     * System variables report for NCSYSVP program.
     *
     * Maps NCSYSVP.NSP (lines 20-33):
     *   READ (10) NCCRUISE
     *     FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
     *       MOVE NCYACHT.YACHT-NAME TO #YACHT-NAME  (A10 - truncated!)
     *     END-FIND
     *     DISPLAY #YACHT-NAME ... (same fields as NCDEDISP)
     *
     * @param page page number (0-based)
     * @param size page size (default 10 matching Natural READ (10))
     * @return paginated list of system variable items
     */
    @Transactional(readOnly = true)
    public Page<SystemVariablesDto> getSystemVariablesReport(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Cruise> cruisePage = cruiseRepository.findAll(pageable);

        List<SystemVariablesDto> items = new ArrayList<>();
        for (Cruise cruise : cruisePage.getContent()) {
            SystemVariablesDto item = new SystemVariablesDto();
            item.setCruiseId(cruise.getCruiseId());

            // NCSYSVP.NSP line 27: NCCRUISE.START-DATE (EM=9999'-'99'-'99)
            item.setStartDate(formatService.formatDate(cruise.getStartDate()));

            // NCSYSVP.NSP line 28: NCCRUISE.START-HARBOR (AL=10)
            item.setStartHarbor(cruise.getStartHarbor() != null ? cruise.getStartHarbor() : "");

            // NCSYSVP.NSP line 29: NCCRUISE.END-DATE (EM=9999'-'99'-'99)
            item.setEndDate(formatService.formatDate(cruise.getEndDate()));

            // NCSYSVP.NSP line 30: NCCRUISE.DESTINATION-HARBOR (AL=10)
            item.setDestinationHarbor(cruise.getDestinationHarbor() != null ? cruise.getDestinationHarbor() : "");

            // NCSYSVP.NSP line 31: NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99)
            item.setPrice1w(formatService.formatPrice(cruise.getPrice1w()));

            // NCSYSVP.NSP lines 21-23: FIND NCYACHT ... MOVE YACHT-NAME TO #YACHT-NAME (A10)
            // #YACHT-NAME is A10, so truncate to 10 characters
            String yachtName = resolveYachtName(cruise.getIdYacht());
            if (yachtName.length() > 10) {
                yachtName = yachtName.substring(0, 10);
            }
            item.setYachtName(yachtName);

            items.add(item);
        }

        return new PageImpl<>(items, pageable, cruisePage.getTotalElements());
    }

    /**
     * Resolves yacht name from yacht ID.
     * Maps: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT / MOVE YACHT-NAME TO ...
     */
    private String resolveYachtName(Long yachtId) {
        if (yachtId == null) {
            return "";
        }
        Optional<Yacht> yachtOpt = yachtRepository.findById(yachtId);
        return yachtOpt.map(Yacht::getYachtName).orElse("");
    }
}
