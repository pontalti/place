package com.demo.place.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;

@Mapper(componentModel = "spring")
public interface PlaceMapper {

    PlaceRecord toRecord(Place entity);

    List<PlaceRecord> toRecord(List<Place> placeList);

    DayOpeningRecord toRecord(DayOpening entity);

    Place toEntity(PlaceRecord record);

    List<Place> toEntity(List<PlaceRecord> recordList);

    @Mapping(target = "place", ignore = true)
    DayOpening toEntity(DayOpeningRecord record);

    /**
     * Named apart from {@code toEntity} on purpose: {@code List<PlaceRecord>}
     * and {@code List<DayOpeningRecord>} erase to the same signature, so the two
     * overloads cannot coexist.
     */
    List<DayOpening> toDayEntity(List<DayOpeningRecord> records);

    /**
     * Copies the scalar fields of a patch onto a managed Place.
     *
     * <p>{@code days} is ignored on purpose: letting MapStruct map it would call
     * setDays and replace the whole collection, bypassing the merge in
     * PlaceServiceImpl#applyDayPatches — the slots carrying an id would be
     * dropped rather than updated.
     */
    @Mapping(target = "days", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromPatch(PlacePatchRecord patch, @MappingTarget Place entity);

    @Mapping(target = "place", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateDayFromPatch(DayOpeningRecord src, @MappingTarget DayOpening tgt);
}