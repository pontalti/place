package com.demo.place.records;

import java.util.List;

public record PlacePatchRecord(
        String label,
        String location,
        List<DayOpeningRecord> days
) {

}
