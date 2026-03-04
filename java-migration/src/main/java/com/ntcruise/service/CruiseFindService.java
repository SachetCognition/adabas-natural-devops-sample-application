package com.ntcruise.service;

import com.ntcruise.dto.CruiseDetailDto;
import com.ntcruise.exception.CruiseNotFoundException;
import com.ntcruise.model.Cruise;
import com.ntcruise.model.CruiseStatus;
import com.ntcruise.model.Yacht;
import com.ntcruise.repository.CruiseRepository;
import com.ntcruise.repository.YachtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service implementing NCFINDCR.NSN subprogram logic.
 * Core data retrieval: finds cruise + yacht by cruise ID.
 *
 * Original Natural source: Subprograms/NCFINDCR.NSN
 *
 * Flow:
 * 1. FIND NCCRUISE CRUISE-ID = #CR-ID-FIND (line 14)
 * 2. IF NO RECORDS FOUND -> RESET NC-PARMS, ESCAPE ROUTINE (lines 15-17)
 * 3. MOVE fields to PDA (lines 21-29)
 * 4. Format dates with EM=9999'-'99'-'99 (lines 23, 25)
 * 5. COMPRESS times with 'h' suffix (lines 24, 26)
 * 6. Format prices with EM=*EUR' 'ZZZZ9.99 (lines 31-33)
 * 7. DECIDE ON FIRST VALUE for status mapping (lines 36-43)
 * 8. Inner FIND NCYACHT for yacht name (lines 46-48)
 */
@Service
public class CruiseFindService {

    private final CruiseRepository cruiseRepository;
    private final YachtRepository yachtRepository;
    private final NaturalFormatService formatService;

    public CruiseFindService(CruiseRepository cruiseRepository,
                             YachtRepository yachtRepository,
                             NaturalFormatService formatService) {
        this.cruiseRepository = cruiseRepository;
        this.yachtRepository = yachtRepository;
        this.formatService = formatService;
    }

    /**
     * Find a cruise by ID and return formatted detail DTO.
     * Maps CALLNAT 'NCFINDCR' NC-PARMS (called from NCINMAPP.NSP line 80).
     *
     * @param cruiseId the cruise ID to find (maps to #CR-ID-FIND)
     * @return CruiseDetailDto with all formatted fields (maps to NC-PARMS output)
     * @throws CruiseNotFoundException if no cruise found (maps to RESET NC-PARMS + ESCAPE ROUTINE)
     */
    @Transactional(readOnly = true)
    public CruiseDetailDto findCruiseById(Long cruiseId) {
        // NCFINDCR.NSN line 14: FIND NCCRUISE CRUISE-ID = #CR-ID-FIND
        Optional<Cruise> cruiseOpt = cruiseRepository.findById(cruiseId);

        // NCFINDCR.NSN lines 15-17: IF NO RECORDS FOUND / RESET NC-PARMS / ESCAPE ROUTINE
        if (cruiseOpt.isEmpty()) {
            throw new CruiseNotFoundException(cruiseId);
        }

        Cruise cruise = cruiseOpt.get();
        CruiseDetailDto dto = new CruiseDetailDto();

        // NCFINDCR.NSN line 21: MOVE NCCRUISE.CRUISE-ID TO #CR-ID
        dto.setCruiseId(cruise.getCruiseId());

        // NCFINDCR.NSN line 23: MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
        dto.setStartDate(formatService.formatDate(cruise.getStartDate()));

        // NCFINDCR.NSN line 24: COMPRESS NCCRUISE.START-TIME 'h' INTO #CR-ST
        dto.setStartTime(formatService.formatTime(cruise.getStartTime()));

        // NCFINDCR.NSN line 25: MOVE EDITED NCCRUISE.END-DATE (EM=9999'-'99'-'99) TO #CR-ED
        dto.setEndDate(formatService.formatDate(cruise.getEndDate()));

        // NCFINDCR.NSN line 26: COMPRESS NCCRUISE.END-TIME 'h' INTO #CR-ET
        dto.setEndTime(formatService.formatTime(cruise.getEndTime()));

        // NCFINDCR.NSN line 28: MOVE NCCRUISE.START-HARBOR TO #CR-FROMH
        dto.setFromHarbor(cruise.getStartHarbor() != null ? cruise.getStartHarbor() : "");

        // NCFINDCR.NSN line 29: MOVE NCCRUISE.DESTINATION-HARBOR TO #CR-TOH
        dto.setToHarbor(cruise.getDestinationHarbor() != null ? cruise.getDestinationHarbor() : "");

        // NCFINDCR.NSN lines 31-33: MOVE EDITED prices with EM=*EUR' 'ZZZZ9.99
        dto.setPrice1w(formatService.formatPrice(cruise.getPrice1w()));
        dto.setPrice2w(formatService.formatPrice(cruise.getPrice2w()));
        dto.setPrice3w(formatService.formatPrice(cruise.getPrice3w()));

        // NCFINDCR.NSN lines 36-43: DECIDE ON FIRST VALUE OF #CR-STATUS
        dto.setStatus(CruiseStatus.toDisplayName(cruise.getCruiseStatus()));

        // NCFINDCR.NSN lines 46-48: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
        //   MOVE YACHT-NAME TO #CR-YACHT-NAME
        String yachtName = "";
        if (cruise.getIdYacht() != null) {
            Optional<Yacht> yachtOpt = yachtRepository.findById(cruise.getIdYacht());
            if (yachtOpt.isPresent()) {
                yachtName = yachtOpt.get().getYachtName();
            }
        }
        dto.setYachtName(yachtName);

        return dto;
    }
}
