package com.donorconnect.dto;

public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long donorCount;
    private long requesterCount;
    private long adminCount;
    private long totalRequests;
    private long openRequests;
    private long fulfilledRequests;
    private long cancelledRequests;
    private long totalReports;
    private long openReports;
    private long resolvedReports;

    public AdminDashboardResponse() {}

    public AdminDashboardResponse(long totalUsers, long activeUsers, long donorCount, long requesterCount, long adminCount, long totalRequests, long openRequests, long fulfilledRequests, long cancelledRequests, long totalReports, long openReports, long resolvedReports) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.donorCount = donorCount;
        this.requesterCount = requesterCount;
        this.adminCount = adminCount;
        this.totalRequests = totalRequests;
        this.openRequests = openRequests;
        this.fulfilledRequests = fulfilledRequests;
        this.cancelledRequests = cancelledRequests;
        this.totalReports = totalReports;
        this.openReports = openReports;
        this.resolvedReports = resolvedReports;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getDonorCount() {
        return donorCount;
    }

    public void setDonorCount(long donorCount) {
        this.donorCount = donorCount;
    }

    public long getRequesterCount() {
        return requesterCount;
    }

    public void setRequesterCount(long requesterCount) {
        this.requesterCount = requesterCount;
    }

    public long getAdminCount() {
        return adminCount;
    }

    public void setAdminCount(long adminCount) {
        this.adminCount = adminCount;
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getOpenRequests() {
        return openRequests;
    }

    public void setOpenRequests(long openRequests) {
        this.openRequests = openRequests;
    }

    public long getFulfilledRequests() {
        return fulfilledRequests;
    }

    public void setFulfilledRequests(long fulfilledRequests) {
        this.fulfilledRequests = fulfilledRequests;
    }

    public long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public long getTotalReports() {
        return totalReports;
    }

    public void setTotalReports(long totalReports) {
        this.totalReports = totalReports;
    }

    public long getOpenReports() {
        return openReports;
    }

    public void setOpenReports(long openReports) {
        this.openReports = openReports;
    }

    public long getResolvedReports() {
        return resolvedReports;
    }

    public void setResolvedReports(long resolvedReports) {
        this.resolvedReports = resolvedReports;
    }
}
