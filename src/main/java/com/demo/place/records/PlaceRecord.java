package com.demo.place.records;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaceRecord(

        Long id,

        @NotBlank(message = "Label is mandatory")
        String label,

        @NotBlank(message = "Location is mandatory")
        String location,

        @NotNull(message = "OpeningHours is mandatory")
        List<@Valid DayOpeningRecord> days

) {

}
