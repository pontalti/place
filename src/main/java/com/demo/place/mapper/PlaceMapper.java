package com.demo.place.mapper;

import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    PlaceRecord toRecord(Place entity);

    @InheritInverseConfiguration
    Place toEntity(PlaceRecord record);


    @Mapping(target = "place", ignore = true)
    DayOpening toEntity(DayOpeningRecord record);

    DayOpeningRecord toRecord(DayOpening entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateEntityFromPatch(PlacePatchRecord patch, @MappingTarget Place entity);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "place", ignore = true)
    void updateDayFromPatch(DayOpeningRecord src, @MappingTarget DayOpening tgt);

}
