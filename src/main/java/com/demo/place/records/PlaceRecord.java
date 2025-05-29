package com.demo.place.records;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PlaceRecord(

        Long id,

        @NotBlank(message = "Label is mandatory")
        String label,

        @NotBlank(message = "Location is mandatory")
        String location,

        @Valid
        @NotNull(message = "OpeningHours is mandatory")
        List<DayOpeningRecord> days

) {

}
