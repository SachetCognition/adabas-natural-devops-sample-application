package com.ntcruise.dto;

/**
 * DTO mapping the Natural PDA NCDEMAPP (NC-PARMS).
 * Used by NCFINDCR subprogram to return cruise detail data.
 *
 * Maps all fields from NCDEMAPP.NSA:
 *   #CR-ID-FIND     (N08.0) -> cruiseIdFind (input parameter)
 *   #CR-ID          (N08.0) -> cruiseId
 *   #CR-ED          (A013)  -> endDate (formatted as YYYY-MM-DD)
 *   #CR-ET          (A007)  -> endTime (formatted as "HHMMSS h")
 *   #CR-FROMH       (A020)  -> fromHarbor
 *   #CR-P1W         (A020)  -> price1w (formatted with EUR currency)
 *   #CR-P2W         (A020)  -> price2w (formatted with EUR currency)
 *   #CR-P3W         (A020)  -> price3w (formatted with EUR currency)
 *   #CR-SD          (A013)  -> startDate (formatted as YYYY-MM-DD)
 *   #CR-ST          (A007)  -> startTime (formatted as "HHMMSS h")
 *   #CR-STATUS      (A020)  -> status (decoded display name)
 *   #CR-TOH         (A020)  -> toHarbor
 *   #CR-YACHT-NAME  (A020)  -> yachtName
 */
public class CruiseDetailDto {

    private Long cruiseId;
    private String status;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String fromHarbor;
    private String toHarbor;
    private String price1w;
    private String price2w;
    private String price3w;
    private String yachtName;

    public CruiseDetailDto() {
    }

    public Long getCruiseId() {
        return cruiseId;
    }

    public void setCruiseId(Long cruiseId) {
        this.cruiseId = cruiseId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFromHarbor() {
        return fromHarbor;
    }

    public void setFromHarbor(String fromHarbor) {
        this.fromHarbor = fromHarbor;
    }

    public String getToHarbor() {
        return toHarbor;
    }

    public void setToHarbor(String toHarbor) {
        this.toHarbor = toHarbor;
    }

    public String getPrice1w() {
        return price1w;
    }

    public void setPrice1w(String price1w) {
        this.price1w = price1w;
    }

    public String getPrice2w() {
        return price2w;
    }

    public void setPrice2w(String price2w) {
        this.price2w = price2w;
    }

    public String getPrice3w() {
        return price3w;
    }

    public void setPrice3w(String price3w) {
        this.price3w = price3w;
    }

    public String getYachtName() {
        return yachtName;
    }

    public void setYachtName(String yachtName) {
        this.yachtName = yachtName;
    }
}
