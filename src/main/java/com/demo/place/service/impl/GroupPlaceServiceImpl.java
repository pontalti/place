package com.demo.place.service.impl;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.demo.place.annotation.Log;
import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.GroupedOpeningDayRecord;
import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.repository.PlaceReadOnlyRepository;
import com.demo.place.service.GroupPlaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GroupPlaceServiceImpl implements GroupPlaceService {

    private static final String CLOSED = "closed";

    private static final Comparator<DayOpening> BY_DAY_THEN_START =
            Comparator.comparing(DayOpening::getDayOfWeek).thenComparing(DayOpening::getStartTime);

    private final List<DayOfWeek> dayOrder;
    private final PlaceReadOnlyRepository placeReadOnlyRepository;

    @Log
    @Override
    @Transactional(readOnly = true)
    public GroupedPlaceRecord getGroupedOpeningHoursByPlaceId(Long id) {
        var place = this.placeReadOnlyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Place not found: " + id));
        /*
         * The entity graph already brought the days along with the place, so
         * the dedicated interval query this method used to run was a second
         * round trip for data that was sitting right here.
         */
        var byDay = buildMapByDay(place);

        Map<List<String>, List<DayOfWeek>> groups = new LinkedHashMap<>();
        this.dayOrder.forEach(day -> {
            var intervals = byDay.getOrDefault(day, List.of());
            var key = intervals.isEmpty() ? List.of(CLOSED) : intervals;
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(day);
        });

        return new GroupedPlaceRecord(
                place.getId(),
                place.getLabel(),
                place.getLocation(),
                buildOpeningGroups(groups));
    }

    /**
     * Groups the intervals of a place by day of the week.
     *
     * <p>Sorting happens here instead of in an ORDER BY because the collection
     * is already in memory — a handful of rows per place.
     */
    private Map<DayOfWeek, List<String>> buildMapByDay(Place place) {
        Map<DayOfWeek, List<String>> byDay = new EnumMap<>(DayOfWeek.class);
        this.dayOrder.forEach(day -> byDay.put(day, new ArrayList<>()));

        place.getDays().stream()
                .sorted(BY_DAY_THEN_START)
                .forEach(day -> byDay
                        .computeIfAbsent(day.getDayOfWeek(), ignored -> new ArrayList<>())
                        .add(day.getStartTime() + " - " + day.getEndTime()));

        return byDay;
    }

    /**
     * Uses the interval list itself as the grouping key.
     *
     * <p>The previous version joined the intervals into a single string and
     * kept a second map to get the list back. List equality is by value, so one
     * map is enough — and a comma inside an interval can no longer make two
     * different schedules collide.
     */
    private List<GroupedOpeningDayRecord> buildOpeningGroups(Map<List<String>, List<DayOfWeek>> groups) {
        return groups.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        entry -> this.dayOrder.indexOf(entry.getValue().getFirst())))
                .map(entry -> new GroupedOpeningDayRecord(
                        formatDays(entry.getValue()), entry.getKey()))
                .toList();
    }

    protected String formatDay(DayOfWeek day) {
        var name = day.name().toLowerCase();
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    protected String formatDays(List<DayOfWeek> days) {
        if (days.size() == 1) {
            return formatDay(days.getFirst());
        }
        return formatDay(days.getFirst()) + " - " + formatDay(days.getLast());
    }
}