package com.ntcruise.dto;

/**
 * DTO for NCSYSVP.NSP (system variables demo) output.
 * Similar to CruiseDisplayItemDto but with truncated yacht name (A10).
 *
 * From NCSYSVP.NSP (lines 25-31):
 *   #YACHT-NAME (A10) - truncated to 10 chars from NCYACHT.YACHT-NAME
 *   NCCRUISE.START-DATE (EM=9999'-'99'-'99)
 *   NCCRUISE.START-HARBOR (AL=10)
 *   NCCRUISE.END-DATE (EM=9999'-'99'-'99)
 *   NCCRUISE.DESTINATION-HARBOR (AL=10)
 *   NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99)
 */
public class SystemVariablesDto {

    private Long cruiseId;
    private String yachtName; // truncated to 10 chars per Natural A10 type
    private String startDate;
    private String startHarbor;
    private String endDate;
    private String destinationHarbor;
    private String price1w;

    public SystemVariablesDto() {
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
