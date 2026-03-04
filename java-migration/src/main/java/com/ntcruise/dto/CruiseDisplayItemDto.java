package com.ntcruise.dto;

/**
 * DTO for NCDEDISP.NSP (DISPLAY with edit masks) output.
 * Extends the list item with price information.
 *
 * From NCDEDISP.NSP (lines 22-28):
 *   NCYACHT.YACHT-NAME                           -> yachtName
 *   NCCRUISE.START-DATE (EM=9999'-'99'-'99)      -> startDate
 *   NCCRUISE.START-HARBOR (AL=10)                -> startHarbor
 *   NCCRUISE.END-DATE (EM=9999'-'99'-'99)        -> endDate
 *   NCCRUISE.DESTINATION-HARBOR (AL=10)          -> destinationHarbor
 *   NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99)      -> price1w
 */
public class CruiseDisplayItemDto {

    private Long cruiseId;
    private String yachtName;
    private String startDate;
    private String startHarbor;
    private String endDate;
    private String destinationHarbor;
    private String price1w;

    public CruiseDisplayItemDto() {
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

    public String getStartHarbor() {
        return startHarbor;
    }

    public void setStartHarbor(String startHarbor) {
        this.startHarbor = startHarbor;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDestinationHarbor() {
        return destinationHarbor;
    }

    public void setDestinationHarbor(String destinationHarbor) {
        this.destinationHarbor = destinationHarbor;
    }

    public String getPrice1w() {
        return price1w;
    }

    public void setPrice1w(String price1w) {
        this.price1w = price1w;
    }
}
