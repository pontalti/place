package com.demo.place.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;

/**
 * Maps between the API records and the JPA entities.
 *
 * <p>{@code unmappedTargetPolicy = IGNORE} only silences the warning for target
 * properties that have no source; it does not stop MapStruct from mapping the
 * ones that do. The back-reference and the opening-hours collection are
 * therefore excluded explicitly with {@code @Mapping(ignore = true)} where
 * mapping them would be wrong.
 */
@Mapper(
        componentModel = "jakarta-cdi",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PlaceMapper {

    public PlaceRecord toRecord(Place entity);

    public List<PlaceRecord> toRecord(List<Place> placeList);

    public DayOpeningRecord toRecord(DayOpening entity);

    public Place toEntity(PlaceRecord record);

    public List<Place> toEntity(List<PlaceRecord> recordList);

    /**
     * The back-reference is left to {@code Place#setDays}, which wires it on
     * every slot it receives.
     */
    @Mapping(target = "place", ignore = true)
    public DayOpening toEntity(DayOpeningRecord record);

    /**
     * Named apart from {@code toEntity} on purpose: {@code List<PlaceRecord>}
     * and {@code List<DayOpeningRecord>} erase to the same signature, so the two
     * overloads cannot coexist.
     */
    public List<DayOpening> toDayEntity(List<DayOpeningRecord> records);

    /**
     * Copies the scalar fields of a patch onto a managed Place.
     *
     * <p>{@code days} is ignored on purpose: letting MapStruct map it would call
     * setDays and replace the whole collection, bypassing the merge in
     * PlaceServiceImpl#applyDayPatches — the slots carrying an id would be
     * dropped rather than updated. The same mistake produced duplicated slots in
     * the Spring branch.
     *
     * <p>{@code id} is ignored as well: the aggregate was loaded by that id, and
     * the patch must not be able to point the entity at a different one.
     */
    @Mapping(target = "days", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void updateEntityFromPatch(PlacePatchRecord patch, @MappingTarget Place entity);

    /**
     * Copies a patched slot onto an existing one.
     *
     * <p>{@code NullValuePropertyMappingStrategy.IGNORE} keeps an omitted field
     * at its current value — the PATCH semantics. {@code id} is ignored because
     * it is what matched the slot in the first place, and {@code place} because
     * the slot stays where it is.
     */
    @Mapping(target = "place", ignore = true)
    @Mapping(target = "id", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public void updateDayFromPatch(DayOpeningRecord src, @MappingTarget DayOpening tgt);
}