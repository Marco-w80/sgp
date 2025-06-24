package com.sgp.dto;

import java.time.YearMonth;


public class MonthCountDto {
    private final int year;
    private final int month;
    private final long count;

    public MonthCountDto(int year, int month, long count) {
        this.year  = year;
        this.month = month;
        this.count = count;
    }

    public int getYear()   { return year; }
    public int getMonth()  { return month; }
    public long getCount() { return count; }
}
