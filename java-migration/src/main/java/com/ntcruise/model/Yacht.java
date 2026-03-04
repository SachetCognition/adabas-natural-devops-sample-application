package com.ntcruise.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * JPA entity mapping NCYACHT DDM (Adabas DBID 012, File 042).
 *
 * Field mapping from NCYACHT.NSD:
 *   DB: YACHT-ID      (N8.0)  -> yachtId (BIGINT)
 *   DC: YACHT-NAME    (A30)   -> yachtName (VARCHAR 30)
 *   DD: YACHT-TYPE    (A30)   -> yachtType (VARCHAR 30)
 *   DF: LENGTH        (P3.2)  -> length (DECIMAL 5,2)
 *   DG: WIDTH         (P3.2)  -> width (DECIMAL 5,2)
 *   DH: DRAFT         (P3.2)  -> draft (DECIMAL 5,2)
 *   DI: SAIL-SURFACE  (P3.0)  -> sailSurface (DECIMAL 5,0)
 *   DJ: MOTOR         (P3.0)  -> motor (DECIMAL 5,0)
 *   DK: HEAD-ROOM     (P3.2)  -> headRoom (DECIMAL 5,2)
 *   DL: BUNKS         (P3.0)  -> bunks (DECIMAL 5,0)
 */
@Entity
@Table(name = "yacht")
public class Yacht {

    @Id
    @Column(name = "yacht_id", nullable = false)
    private Long yachtId;

    @Column(name = "yacht_name", nullable = false, length = 30)
    private String yachtName;

    @Column(name = "yacht_type", nullable = false, length = 30)
    private String yachtType;

    @Column(name = "length", precision = 5, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 5, scale = 2)
    private BigDecimal width;

    @Column(name = "draft", precision = 5, scale = 2)
    private BigDecimal draft;

    @Column(name = "sail_surface", precision = 5, scale = 0)
    private BigDecimal sailSurface;

    @Column(name = "motor", precision = 5, scale = 0)
    private BigDecimal motor;

    @Column(name = "head_room", precision = 5, scale = 2)
    private BigDecimal headRoom;

    @Column(name = "bunks", precision = 5, scale = 0)
    private BigDecimal bunks;

    public Yacht() {
    }

    public Long getYachtId() {
        return yachtId;
    }

    public void setYachtId(Long yachtId) {
        this.yachtId = yachtId;
    }

    public String getYachtName() {
        return yachtName;
    }

    public void setYachtName(String yachtName) {
        this.yachtName = yachtName;
    }

    public String getYachtType() {
        return yachtType;
    }

    public void setYachtType(String yachtType) {
        this.yachtType = yachtType;
    }

    public BigDecimal getLength() {
        return length;
    }

    public void setLength(BigDecimal length) {
        this.length = length;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public BigDecimal getDraft() {
        return draft;
    }

    public void setDraft(BigDecimal draft) {
        this.draft = draft;
    }

    public BigDecimal getSailSurface() {
        return sailSurface;
    }

    public void setSailSurface(BigDecimal sailSurface) {
        this.sailSurface = sailSurface;
    }

    public BigDecimal getMotor() {
        return motor;
    }

    public void setMotor(BigDecimal motor) {
        this.motor = motor;
    }

    public BigDecimal getHeadRoom() {
        return headRoom;
    }

    public void setHeadRoom(BigDecimal headRoom) {
        this.headRoom = headRoom;
    }

    public BigDecimal getBunks() {
        return bunks;
    }

    public void setBunks(BigDecimal bunks) {
        this.bunks = bunks;
    }
}
