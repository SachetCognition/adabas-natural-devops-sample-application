package com.ntcruise.dto;

/**
 * DTO for NCWRFORP.NSP (WRITE USING FORM) output.
 * Maps the NCDEFORM map fields with their initial values.
 *
 * From NCWRFORP.NSP (lines 14-27):
 *   #CR-ED          (A013) INIT <'2015-08-01'>       -> endDate
 *   #CR-ET          (A007) INIT <'7 h'>              -> endTime
 *   #CR-FROMH       (A020) INIT <'Samos'>            -> fromHarbor
 *   #CR-ID          (N08.0) INIT <12345678>          -> cruiseId
 *   #CR-P1W         (A020) INIT <'1000.00'>          -> price1w
 *   #CR-P2W         (A020) INIT <'2000.00'>          -> price2w
 *   #CR-P3W         (A020) INIT <'3000.00'>          -> price3w
 *   #CR-SD          (A013) INIT <'2015-08-20'>       -> startDate
 *   #CR-ST          (A007) INIT <'10 h'>             -> startTime
 *   #CR-STATUS      (A020) INIT <'available'>        -> status
 *   #CR-TOH         (A020) INIT <'Santorini'>        -> toHarbor
 *   #CR-YACHT-NAME  (A020) INIT <'Cassandra'>        -> yachtName
 */
public class CruiseFormDto {

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

    public CruiseFormDto() {
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
