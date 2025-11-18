package com.demo.place.mapper;

import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import org.mapstruct.*;

@Mapper(
    componentModel = "cdi",
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlaceMapper {

    PlaceRecord toRecord(Place entity);

    DayOpeningRecord toRecord(DayOpening entity);

    Place toEntity(PlaceRecord record);

    DayOpening toEntity(DayOpeningRecord record);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(PlacePatchRecord patch, @MappingTarget Place entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDayFromPatch(DayOpeningRecord src, @MappingTarget DayOpening tgt);
}
