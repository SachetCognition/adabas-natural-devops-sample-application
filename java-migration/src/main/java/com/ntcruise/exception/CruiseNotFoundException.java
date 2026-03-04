package com.ntcruise.exception;

/**
 * Exception thrown when a cruise lookup fails.
 * Maps the Natural pattern from NCFINDCR.NSN (lines 15-17):
 *   IF NO RECORDS FOUND
 *     RESET NC-PARMS
 *     ESCAPE ROUTINE
 *
 * And from NCINMAPP.NSP (lines 81-82):
 *   IF #CR-ID-FIND = 0
 *     REINPUT FULL 'Sorry - No Cruise found for Id'
 */
public class CruiseNotFoundException extends RuntimeException {

    private final Long cruiseId;

    public CruiseNotFoundException(Long cruiseId) {
        super("Sorry - No Cruise found for Id " + cruiseId);
        this.cruiseId = cruiseId;
    }

    public Long getCruiseId() {
        return cruiseId;
    }
}
