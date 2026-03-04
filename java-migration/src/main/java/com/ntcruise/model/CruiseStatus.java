package com.ntcruise.model;

/**
 * Enum mapping for CRUISE-STATUS field from NCCRUISE DDM.
 * Maps the DECIDE ON FIRST VALUE logic from NCFINDCR.NSN (lines 36-43).
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
public enum CruiseStatus {

    REMOVED("0", "removed"),
    PLANNED("1", "planned"),
    AVAILABLE("2", "available"),
    SOLD("3", "sold");

    private final String code;
    private final String displayName;

    CruiseStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Lookup by Adabas status code. Returns display name.
     * Handles NONE case by returning "unknown" for unmatched codes,
     * exactly matching the Natural DECIDE ON FIRST VALUE behavior.
     *
     * @param code the single-character status code from CRUISE-STATUS field
     * @return the display name ("removed", "planned", "available", "sold", or "unknown")
     */
    public static String toDisplayName(String code) {
        for (CruiseStatus status : values()) {
            if (status.code.equals(code)) {
                return status.displayName;
            }
        }
        return "unknown"; // NONE case from NCFINDCR.NSN line 42
    }

    /**
     * Lookup enum by Adabas status code.
     *
     * @param code the single-character status code
     * @return the CruiseStatus enum, or null if not found
     */
    public static CruiseStatus fromCode(String code) {
        for (CruiseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;
    }
}
