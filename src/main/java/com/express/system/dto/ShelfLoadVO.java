package com.express.system.dto;

import java.math.BigDecimal;

public class ShelfLoadVO {

    private Long id;
    private Integer shelfCode;
    private Integer shelfLayer;
    private String shelfName;
    private Byte shelfType;
    private Byte status;
    private Integer currentUsage;
    private Integer totalCapacity;
    private BigDecimal loadRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getShelfCode() {
        return shelfCode;
    }

    public void setShelfCode(Integer shelfCode) {
        this.shelfCode = shelfCode;
    }

    public Integer getShelfLayer() {
        return shelfLayer;
    }

    public void setShelfLayer(Integer shelfLayer) {
        this.shelfLayer = shelfLayer;
    }

    public String getShelfName() {
        return shelfName;
    }

    public void setShelfName(String shelfName) {
        this.shelfName = shelfName;
    }

    public Byte getShelfType() {
        return shelfType;
    }

    public void setShelfType(Byte shelfType) {
        this.shelfType = shelfType;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Integer getCurrentUsage() {
        return currentUsage;
    }

    public void setCurrentUsage(Integer currentUsage) {
        this.currentUsage = currentUsage;
    }

    public Integer getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(Integer totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public BigDecimal getLoadRate() {
        return loadRate;
    }

    public void setLoadRate(BigDecimal loadRate) {
        this.loadRate = loadRate;
    }
}
