package com.demo.place.records;

import java.util.List;

public record PlacePatchRecord(
		Long id,
        String label,
        String location,
        List<DayOpeningRecord> days
) {

}
