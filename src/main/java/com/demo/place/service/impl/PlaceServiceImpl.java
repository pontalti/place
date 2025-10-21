package com.demo.place.service.impl;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.demo.place.aop.Log;
import com.demo.place.entity.Place;
import com.demo.place.mapper.PlaceMapper;
import com.demo.place.records.PlacePatchRecord;
import com.demo.place.records.PlaceRecord;
import com.demo.place.repository.PlaceRepository;
import com.demo.place.service.PlaceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PlaceServiceImpl implements PlaceService {

    private final PlaceRepository repository;
    private final PlaceMapper mapper;

    @Log
    @Override
    public List<PlaceRecord> savePlace(List<PlaceRecord> places) {
        var entities = places.stream()
                .map(this::buildEntity)
                .collect(Collectors.toList());
        List<Place> saved = this.repository.saveAll(entities);
        return saved.stream()
                .map(this.mapper::toRecord)
                .collect(Collectors.toList());
    }

    @Log
    @Override
    public List<PlaceRecord> listAll() {
        return this.repository.findAll().stream()
                .map(this.mapper::toRecord)
                .collect(Collectors.toList());
    }

    @Log
    @Override
    public PlaceRecord findById(Long id) {
        var place = this.repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Place not found: " + id));
        return this.mapper.toRecord(place);
    }

    @Log
    @Override
    public void deleteById(Long id) {
        if (!this.repository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Place not found: " + id);
        }
        this.repository.deleteById(id);
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord updatePlace(PlaceRecord updatedPlace) {
        var existingPlace = this.repository.findById(updatedPlace.id())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));

        existingPlace.setLabel(updatedPlace.label());
        existingPlace.setLocation(updatedPlace.location());

        existingPlace.getDays().clear();
        var newDays = updatedPlace.days().stream().map(d -> {
            var day = this.mapper.toEntity(d);
            day.setPlace(existingPlace);
            return day;
        }).toList();

        existingPlace.getDays().addAll(newDays);

        var savedPlace = this.repository.save(existingPlace);
        return this.mapper.toRecord(savedPlace);
    }

    protected Place buildEntity(PlaceRecord record) {
        var place = this.mapper.toEntity(record);
        place.getDays().forEach(d -> d.setPlace(place));
        return place;
    }

    @Log
    @Override
    @Transactional
    public PlaceRecord patchPlace(Long id, PlacePatchRecord patch) {
        var place = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Place not found: " + id));

        if (patch.label() != null) {
            place.setLabel(patch.label());
        }

        if (patch.location() != null) {
            place.setLocation(patch.location());
        }

        var newDays = patch.days().stream().filter(rec -> rec.id() == null).map(rec -> {
            var day = this.mapper.toEntity(rec);
            day.setPlace(place);
            return day;
        }).toList();

        place.getDays().addAll(newDays);

        var days = patch.days().stream().filter(rec -> rec.id() != null).toList();

        if (!days.isEmpty()) {
            days.forEach(d -> {
                place.getDays().stream().filter(dayOpening -> Objects.equals(dayOpening.getId(), d.id()))
                        .forEach(dayOpening -> {
                            dayOpening.setDayOfWeek(DayOfWeek.valueOf(d.dayOfWeek()));
                            dayOpening.setType(d.type());
                            dayOpening.setStartTime(d.startTime());
                            dayOpening.setEndTime(d.endTime());
                        });

            });
        }
        return mapper.toRecord(repository.save(place));
    }

}