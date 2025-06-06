package com.demo.place.records;

import java.time.DayOfWeek;

public record DayIntervalRecord(
    DayOfWeek day,
    String startTime,
    String endTime
) {}