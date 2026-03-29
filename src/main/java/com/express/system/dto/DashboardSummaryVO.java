package com.express.system.dto;

public class DashboardSummaryVO {

    private Long totalExpress;
    private Long pendingPickup;
    private Long overdue48h;
    private Long todayCheckin;
    private Long todayCheckout;
    private Long pendingSendOrders;

    public Long getTotalExpress() {
        return totalExpress;
    }

    public void setTotalExpress(Long totalExpress) {
        this.totalExpress = totalExpress;
    }

    public Long getPendingPickup() {
        return pendingPickup;
    }

    public void setPendingPickup(Long pendingPickup) {
        this.pendingPickup = pendingPickup;
    }

    public Long getOverdue48h() {
        return overdue48h;
    }

    public void setOverdue48h(Long overdue48h) {
        this.overdue48h = overdue48h;
    }

    public Long getTodayCheckin() {
        return todayCheckin;
    }

    public void setTodayCheckin(Long todayCheckin) {
        this.todayCheckin = todayCheckin;
    }

    public Long getTodayCheckout() {
        return todayCheckout;
    }

    public void setTodayCheckout(Long todayCheckout) {
        this.todayCheckout = todayCheckout;
    }

    public Long getPendingSendOrders() {
        return pendingSendOrders;
    }

    public void setPendingSendOrders(Long pendingSendOrders) {
        this.pendingSendOrders = pendingSendOrders;
    }
}
