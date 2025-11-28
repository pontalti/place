package com.demo.place.records;

import com.demo.place.annotation.StartBeforeEnd;
import com.demo.place.annotation.ValidTime;
import jakarta.validation.constraints.NotBlank;

import java.util.Locale;

@StartBeforeEnd
public record DayOpeningRecord(

        Long id,

        @NotBlank(message = "Day of the week required")
        String dayOfWeek,

        @ValidTime(message = "Start time must be in format HH:mm")
        @NotBlank(message = "Start time is required")
        String startTime,

        @ValidTime(message = "End time must be in format HH:mm")
        @NotBlank(message = "End time is required")
        String endTime,

        @NotBlank(message = "Type is required")
        String type

) {

    public DayOpeningRecord {
        if (dayOfWeek != null && !dayOfWeek.isBlank()) {
            dayOfWeek = dayOfWeek.toUpperCase(Locale.ROOT);
        }
    }

}