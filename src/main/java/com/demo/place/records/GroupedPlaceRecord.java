package com.demo.place.records;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroupedPlaceRecord(
        Long id,
        String label,
        String location,

        @JsonProperty("openingHours")
        @JsonAlias({"opening_hours"})
        List<GroupedOpeningDayRecord> openingHours
) {
}