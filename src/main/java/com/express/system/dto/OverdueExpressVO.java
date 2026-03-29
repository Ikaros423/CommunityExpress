package com.express.system.dto;

import java.time.LocalDateTime;

public class OverdueExpressVO {

    private Long id;
    private String trackingNumber;
    private String receiverName;
    private String receiverPhone;
    private Integer shelfCode;
    private Integer shelfLayer;
    private LocalDateTime createTime;
    private Long overdueDays;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
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

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public Long getOverdueDays() {
        return overdueDays;
    }

    public void setOverdueDays(Long overdueDays) {
        this.overdueDays = overdueDays;
    }
}
