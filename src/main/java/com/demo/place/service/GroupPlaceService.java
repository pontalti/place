package com.demo.place.service;

import com.demo.place.records.GroupedPlaceRecord;

public interface GroupPlaceService {

    public GroupedPlaceRecord getGroupedOpeningHoursByPlaceId(Long id);

}
