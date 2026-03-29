package com.express.system.dto;

public class DashboardTrendPointVO {

    private String date;
    private Long checkinCount;
    private Long checkoutCount;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getCheckinCount() {
        return checkinCount;
    }

    public void setCheckinCount(Long checkinCount) {
        this.checkinCount = checkinCount;
    }

    public Long getCheckoutCount() {
        return checkoutCount;
    }

    public void setCheckoutCount(Long checkoutCount) {
        this.checkoutCount = checkoutCount;
    }
}
