package com.demo.place.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.demo.place.annotation.Log;
import com.demo.place.entity.Place;
import com.demo.place.mapper.PlaceMapper;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.repository.PlaceReadOnlyRepository;
import com.demo.place.repository.PlaceWriteRepository;
import com.demo.place.service.PlaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceWriteRepository placeWriteRepository;
    private final PlaceReadOnlyRepository placeReadOnlyRepository;
    private final PlaceMapper mapper;

    @Log
    @Override
    @Transactional
    public PlaceRecord savePlace(PlaceRecord place) {
        var savedPlace = this.placeWriteRepository.save(buildEntity(place));
        return this.mapper.toRecord(savedPlace);
    }

    @Log
    @Override
    @Transactional
    public List<PlaceRecord> savePlace(List<PlaceRecord> places) {
        /*
         * One transaction for the whole batch: without it each save commits on
         * its own, and a failure halfway through leaves the earlier rows behind.
         */
        var savedPlaces = this.placeWriteRepository.saveAll(buildEntity(places));
        return this.mapper.toRecord(savedPlaces);
    }

    @Log
    @Override
    @Transactional(readOnly = true)
    public List<PlaceRecord> listAll() {
        return this.placeReadOnlyRepository.findAll().stream()
                .map(this.mapper::toRecord)
                .collect(Collectors.toList());
    }

    @Log
    @Override
    @Transactional(readOnly = true)
    public PlaceRecord findById(Long id) {
        var place = this.placeReadOnlyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Place not found: " + id));
        return this.mapper.toRecord(place);
    }

    @Log
    @Override
    @Transactional
    public void deleteById(Long id) {
        /*
         * The check and the delete share a transaction, so the row cannot
         * disappear between them and turn a 404 into an unexpected 500.
         */
        if (!this.placeWriteRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found: " + id);
        }
        this.placeWriteRepository.deleteById(id);
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord updatePlace(PlaceRecord updatedPlace) {
        var existingPlace = loadOrThrow(updatedPlace.id());
        existingPlace.setLabel(updatedPlace.label());
        existingPlace.setLocation(updatedPlace.location());
        /*
         * setDays clears the managed collection and wires the back-reference on
         * each slot, which is what keeps orphanRemoval working.
         */
        existingPlace.setDays(this.mapper.toDayEntity(updatedPlace.days()));

        return this.mapper.toRecord(this.placeWriteRepository.save(existingPlace));
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord patchPlace(PlacePatchRecord patch) {
        var place = loadOrThrow(patch.id());

        if (patch.label() != null) {
            place.setLabel(patch.label());
        }
        if (patch.location() != null) {
            place.setLocation(patch.location());
        }

        applyDayPatches(place, patch.days());

        return this.mapper.toRecord(this.placeWriteRepository.save(place));
    }

    @Log
    @Override
    @Transactional(readOnly = true)
    public Page<PlaceRecord> listAll(Pageable pageable) {
        var ids = this.placeReadOnlyRepository.findPlaceIds(pageable);

        if (ids.isEmpty()) {
            /*
             * Not Page.empty(pageable): that reports totalElements as 0, so a
             * page index past the end would tell the client the table is empty.
             */
            return new PageImpl<>(List.of(), pageable, ids.getTotalElements());
        }

        var places = this.placeReadOnlyRepository
                .findAllByIdIn(ids.getContent(), pageable.getSort());

        return new PageImpl<>(places.stream().map(this.mapper::toRecord).toList(), 
        											pageable,
        											ids.getTotalElements());
    }

    private Place loadOrThrow(Long id) {
        return this.placeWriteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Place not found: " + id));
    }

    /**
     * Applies the day patches: slots carrying an id update the matching one,
     * the rest are appended.
     *
     * <p>{@code days} is optional in a PATCH — the payload only has to carry one
     * updatable field — so a null list means "leave the opening hours alone".
     * Streaming it unguarded used to throw an NPE on {@code {"id": 1, "label": "X"}}.
     *
     * <p>The updates are indexed by id first, so this is a single pass over the
     * existing slots instead of a nested scan per patched entry.
     *
     * <p>The field copy goes through the mapper's {@code updateDayFromPatch},
     * whose {@code NullValuePropertyMappingStrategy.IGNORE} leaves an omitted
     * field at its current value — the PATCH semantics.
     *
     * <p>The merged list is handed to {@code setDays}, which mutates the managed
     * collection and wires the back-reference on every slot; adding to
     * {@code getDays()} directly would leave {@code place_id} null on the new
     * rows.
     */
    private void applyDayPatches(Place place, List<DayOpeningRecord> patchedDays) {
        if (patchedDays == null || patchedDays.isEmpty()) {
            return;
        }

        Map<Long, DayOpeningRecord> updatesById = patchedDays.stream()
                .filter(day -> day.id() != null)
                .collect(Collectors.toMap(DayOpeningRecord::id, Function.identity(), (first, last) -> last));

        var merged = new ArrayList<>(place.getDays());

        merged.stream()
                .filter(day -> updatesById.containsKey(day.getId()))
                .forEach(day -> this.mapper.updateDayFromPatch(updatesById.get(day.getId()), day));

        patchedDays.stream()
                .filter(day -> day.id() == null)
                .map(this.mapper::toEntity)
                .forEach(merged::add);

        place.setDays(merged);
    }

    protected List<Place> buildEntity(List<PlaceRecord> records) {
        var placeEntityList = this.mapper.toEntity(records);
        placeEntityList.forEach(place -> place.getDays().forEach(day -> day.setPlace(place)));
        return placeEntityList;
    }

    protected Place buildEntity(PlaceRecord record) {
        var placeEntity = this.mapper.toEntity(record);
        placeEntity.getDays().forEach(day -> day.setPlace(placeEntity));
        return placeEntity;
    }
}