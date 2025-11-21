package com.demo.place.service.impl;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.demo.place.annotation.Log;
import com.demo.place.entity.Place;
import com.demo.place.mapper.PlaceMapper;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.repository.PlaceRepository;
import com.demo.place.service.PlaceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository repository;
    private final PlaceMapper mapper;

    @Log
    @Override
    @Transactional
    public List<PlaceRecord> savePlace(List<PlaceRecord> places) {
        var entities = places.stream()
                .map(this::buildEntity)
                .collect(Collectors.toList());

        repository.persist(entities);

        return entities.stream()
                .map(mapper::toRecord)
                .collect(Collectors.toList());
    }

    @Log
    @Override
    public List<PlaceRecord> listAll() {
        return repository.listAll().stream()
                .map(mapper::toRecord)
                .collect(Collectors.toList());
    }

    @Log
    @Override
    public PlaceRecord findById(Long id) {
        Place place = repository.findById(id);
        if (place == null) {
            throw new WebApplicationException(
                    "Place not found: " + id,
                    Response.Status.NOT_FOUND
            );
        }
        return mapper.toRecord(place);
    }

    @Log
    @Override
    @Transactional
    public void deleteById(Long id) {
        boolean deleted = repository.deleteById(id);
        if (!deleted) {
            throw new WebApplicationException(
                    "Place not found: " + id,
                    Response.Status.NOT_FOUND
            );
        }
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord updatePlace(PlaceRecord updatedPlace) {
        if (updatedPlace.id() == null) {
            throw new WebApplicationException(
                    "Place id is required for update",
                    Response.Status.BAD_REQUEST
            );
        }

        Place existingPlace = repository.findById(updatedPlace.id());
        if (existingPlace == null) {
            throw new WebApplicationException(
                    "Place not found: " + updatedPlace.id(),
                    Response.Status.NOT_FOUND
            );
        }
        existingPlace.setLabel(updatedPlace.label());
        existingPlace.setLocation(updatedPlace.location());

        existingPlace.getDays().clear();

        var newDays = updatedPlace.days().stream()
                .map(d -> {
                    var day = mapper.toEntity(d);
                    day.setPlace(existingPlace);
                    return day;
                })
                .toList();

        existingPlace.getDays().addAll(newDays);

        return mapper.toRecord(existingPlace);
    }


    protected Place buildEntity(PlaceRecord record) {
        var place = mapper.toEntity(record);
        place.getDays().forEach(d -> d.setPlace(place));
        return place;
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord patchPlace(PlacePatchRecord patch) {
        Place place = repository.findById(patch.id());
        if (place == null) {
            throw new WebApplicationException(
                    "Place not found: " + patch.id(),
                    Response.Status.NOT_FOUND
            );
        }

        if (patch.label() != null) {
            place.setLabel(patch.label());
        }
        if (patch.location() != null) {
            place.setLocation(patch.location());
        }

        var newDays = patch.days().stream()
                .filter(rec -> rec.id() == null)
                .map(rec -> {
                    var day = mapper.toEntity(rec);
                    day.setPlace(place);
                    return day;
                })
                .toList();

        place.getDays().addAll(newDays);

        var daysToUpdate = patch.days().stream()
                .filter(rec -> rec.id() != null)
                .toList();

        if (!daysToUpdate.isEmpty()) {
            daysToUpdate.forEach(d -> {
                place.getDays().stream()
                        .filter(dayOpening -> Objects.equals(dayOpening.getId(), d.id()))
                        .forEach(dayOpening -> {
                            dayOpening.setDayOfWeek(DayOfWeek.valueOf(d.dayOfWeek()));
                            dayOpening.setType(d.type());
                            dayOpening.setStartTime(d.startTime());
                            dayOpening.setEndTime(d.endTime());
                        });
            });
        }

        return mapper.toRecord(place);
    }
}
