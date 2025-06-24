package com.sgp.dto;

import com.sgp.model.StatusProcesso;

public class StatusCountDto {
    private StatusProcesso status;
    private long count;

    public StatusCountDto(StatusProcesso status, long count) {
        this.status = status;
        this.count = count;
    }

    public StatusProcesso getStatus() { return status; }
    public long getCount()       { return count; }
}
