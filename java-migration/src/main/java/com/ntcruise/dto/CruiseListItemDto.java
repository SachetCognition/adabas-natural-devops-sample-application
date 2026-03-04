package com.ntcruise.dto;

/**
 * DTO for cruise list items used in report programs (NCATENDP, NCATTOPP, NCDEDISP).
 * Maps the WRITE/DISPLAY output fields from these Natural programs.
 *
 * From NCATENDP.NSP (lines 40-46):
 *   NCYACHT.YACHT-NAME      -> yachtName (AL=15)
 *   NCCRUISE.START-DATE      -> startDate (EM=9999'-'99'-'99)
 *   NCCRUISE.END-DATE        -> endDate (EM=9999'-'99'-'99)
 *   NCCRUISE.START-HARBOR    -> startHarbor (AL=15)
 *   NCCRUISE.DESTINATION-HARBOR -> destinationHarbor (AL=15)
 */
public class CruiseListItemDto {

    private Long cruiseId;
    private String yachtName;
    private String startDate;
    private String endDate;
    private String startHarbor;
    private String destinationHarbor;

    public CruiseListItemDto() {
    }

    public Long getCruiseId() {
        return cruiseId;
    }

    public void setCruiseId(Long cruiseId) {
        this.cruiseId = cruiseId;
    }

    public String getYachtName() {
        return yachtName;
    }

    public void setYachtName(String yachtName) {
        this.yachtName = yachtName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartHarbor() {
        return startHarbor;
    }

    public void setStartHarbor(String startHarbor) {
        this.startHarbor = startHarbor;
    }

    public String getDestinationHarbor() {
        return destinationHarbor;
    }

    public void setDestinationHarbor(String destinationHarbor) {
        this.destinationHarbor = destinationHarbor;
    }
}
