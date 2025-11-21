package com.demo.place.records;

import java.util.List;

import com.demo.place.annotation.NonEmptyPatch;

import jakarta.validation.constraints.NotNull;

@NonEmptyPatch
public record PlacePatchRecord(

		@NotNull(message = "The ID is required for the PATCH operation.")
		Long id,
		
        String label,
        String location,
        List<DayOpeningRecord> days
) {

}
