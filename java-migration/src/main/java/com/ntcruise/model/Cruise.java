package com.ntcruise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * JPA entity mapping NCCRUISE DDM (Adabas DBID 012, File 041).
 *
 * Field mapping from NCCRUISE.NSD:
 *   CI: CRUISE-ID          (N8.0) -> cruiseId (BIGINT)
 *   CK: CRUISE-STATUS      (A1)   -> cruiseStatus (VARCHAR 1)
 *   Group CL: CRUISE-START
 *     CM: START-DATE        (N8.0) -> startDate (BIGINT, YYYYMMDD)
 *     CN: START-TIME        (N6.0) -> startTime (BIGINT, HHMMSS)
 *   Group CO: CRUISE-END
 *     CP: END-DATE          (N8.0) -> endDate (BIGINT, YYYYMMDD)
 *     CQ: END-TIME          (N6.0) -> endTime (BIGINT, HHMMSS)
 *   CR: START-HARBOR        (A20)  -> startHarbor (VARCHAR 20)
 *   CS: DESTINATION-HARBOR  (A20)  -> destinationHarbor (VARCHAR 20)
 *   CT: ID-YACHT            (N8.0) -> idYacht (FK to yacht)
 *   Group CW: PRICES
 *     CX: PRICE-1W          (P10.3) -> price1w (DECIMAL 12,3)
 *     CY: PRICE-2W          (P10.3) -> price2w (DECIMAL 12,3)
 *     CZ: PRICE-3W          (P10.3) -> price3w (DECIMAL 12,3)
 */
@Entity
@Table(name = "cruise")
public class Cruise {

    @Id
    @Column(name = "cruise_id", nullable = false)
    private Long cruiseId;

    @Column(name = "cruise_status", nullable = false, length = 1)
    private String cruiseStatus;

    // Group CRUISE-START (CL) - flattened
    @Column(name = "start_date")
    private Long startDate;

    @Column(name = "start_time")
    private Long startTime;

    // Group CRUISE-END (CO) - flattened
    @Column(name = "end_date")
    private Long endDate;

    @Column(name = "end_time")
    private Long endTime;

    @Column(name = "start_harbor", length = 20)
    private String startHarbor;

    @Column(name = "destination_harbor", length = 20)
    private String destinationHarbor;

    // FK to yacht - FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT (NCFINDCR.NSN line 46)
    @Column(name = "id_yacht")
    private Long idYacht;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_yacht", referencedColumnName = "yacht_id", insertable = false, updatable = false)
    private Yacht yacht;

    // Group PRICES (CW) - flattened
    @Column(name = "price_1w", precision = 12, scale = 3)
    private BigDecimal price1w;

    @Column(name = "price_2w", precision = 12, scale = 3)
    private BigDecimal price2w;

    @Column(name = "price_3w", precision = 12, scale = 3)
    private BigDecimal price3w;

    public Cruise() {
    }

    public Long getCruiseId() {
        return cruiseId;
    }

    public void setCruiseId(Long cruiseId) {
        this.cruiseId = cruiseId;
    }

    public String getCruiseStatus() {
        return cruiseStatus;
    }

    public void setCruiseStatus(String cruiseStatus) {
        this.cruiseStatus = cruiseStatus;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
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

    public Long getIdYacht() {
        return idYacht;
    }

    public void setIdYacht(Long idYacht) {
        this.idYacht = idYacht;
    }

    public Yacht getYacht() {
        return yacht;
    }

    public void setYacht(Yacht yacht) {
        this.yacht = yacht;
    }

    public BigDecimal getPrice1w() {
        return price1w;
    }

    public void setPrice1w(BigDecimal price1w) {
        this.price1w = price1w;
    }

    public BigDecimal getPrice2w() {
        return price2w;
    }

    public void setPrice2w(BigDecimal price2w) {
        this.price2w = price2w;
    }

    public BigDecimal getPrice3w() {
        return price3w;
    }

    public void setPrice3w(BigDecimal price3w) {
        this.price3w = price3w;
    }
}
