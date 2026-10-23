package com.school.dto;

/**
 * 未读数
 */
public class UnreadCountResponse {

    private long total;

    public UnreadCountResponse() {
    }

    public UnreadCountResponse(long total) {
        this.total = total;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }
}
