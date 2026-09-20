package com.demo.place.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.demo.place.annotation.Log;
import com.demo.place.entity.Place;
import com.demo.place.mapper.PlaceMapper;
import com.demo.place.records.DayOpeningRecord;
import com.demo.place.records.PageRequest;
import com.demo.place.records.PageResponse;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.repository.PlaceReadOnlyRepository;
import com.demo.place.repository.PlaceWriteRepository;
import com.demo.place.service.PlaceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

/**
 * Reads go through {@link PlaceReadOnlyRepository}; anything that loads a place
 * in order to change it goes through {@link PlaceWriteRepository}.
 *
 * <p>The split is not cosmetic here: the read-only repository loads entities
 * with Hibernate's read-only hint, so a change made to one of them is never
 * flushed. Loading through it in update or patch would return 200 with the new
 * values in the body and write nothing to the database.
 *
 * <p>{@code jakarta.transaction.Transactional} has no {@code readOnly}
 * attribute, which is why the read methods carry no annotation at all: the
 * read-only hint on the repository plays that role, and Quarkus opens a
 * request-scoped session for queries without a transaction.
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceWriteRepository writeRepository;
    private final PlaceReadOnlyRepository readOnlyRepository;
    private final PlaceMapper mapper;

    @Log
    @Override
    @Transactional
    public PlaceRecord savePlace(PlaceRecord place) {
        var savedPlace = this.writeRepository.save(buildEntity(place));
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
        var savedPlaces = this.writeRepository.save(buildEntity(places));
        return this.mapper.toRecord(savedPlaces);
    }

    @Log
    @Override
    public List<PlaceRecord> listAll() {
        return this.readOnlyRepository.listAll().stream()
                .map(this.mapper::toRecord)
                .toList();
    }

    @Log
    @Override
    public PlaceRecord findById(Long id) {
        return this.readOnlyRepository.findById(id)
                .map(this.mapper::toRecord)
                .orElseThrow(() -> notFound(id));
    }

    @Log
    @Override
    @Transactional
    public void deleteById(Long id) {
        /*
         * The lookup and the delete happen inside the repository, in this
         * transaction, so the row cannot disappear between them and turn a 404
         * into an unexpected 500.
         */
        if (!this.writeRepository.deleteById(id)) {
            throw notFound(id);
        }
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord updatePlace(PlaceRecord updatedPlace) {
        if (updatedPlace.id() == null) {
            throw badRequest("Place id is required for update");
        }

        var existingPlace = loadOrThrow(updatedPlace.id());
        existingPlace.setLabel(updatedPlace.label());
        existingPlace.setLocation(updatedPlace.location());
        /*
         * setDays clears the managed collection and wires the back-reference on
         * each slot, which is what keeps orphanRemoval working.
         */
        existingPlace.setDays(this.mapper.toDayEntity(updatedPlace.days()));

        return this.mapper.toRecord(this.writeRepository.save(existingPlace));
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

        return this.mapper.toRecord(this.writeRepository.save(place));
    }

    /**
     * Pages over the places.
     *
     * <p>Two round trips instead of one: the first pages the ids, the second
     * loads that page with its opening hours. See
     * {@link PlaceReadOnlyRepository#findPageOfIds} for why a single query with
     * the fetch graph cannot page correctly.
     */
    @Log
    @Override
    public PageResponse<PlaceRecord> listAll(PageRequest page) {
        long total = this.readOnlyRepository.countAll();

        List<Long> ids = this.readOnlyRepository.findPageOfIds(page);
        if (ids.isEmpty()) {
            /*
             * Keeps the real total: an empty page would otherwise tell the
             * client, for an index past the end, that the table is empty.
             */
            return PageResponse.empty(page.number(), page.size(), total);
        }

        var places = this.readOnlyRepository.findAllByIdIn(ids, page);

        return PageResponse.of(
                places.stream().map(this.mapper::toRecord).toList(),
                page.number(),
                page.size(),
                total);
    }

    /** Loads through the write repository: the entity is tracked and its changes flushed. */
    private Place loadOrThrow(Long id) {
        return this.writeRepository.findById(id).orElseThrow(() -> notFound(id));
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

    private static WebApplicationException notFound(Long id) {
        return new WebApplicationException("Place not found: " + id, Response.Status.NOT_FOUND);
    }

    private static WebApplicationException badRequest(String message) {
        return new WebApplicationException(message, Response.Status.BAD_REQUEST);
    }
}