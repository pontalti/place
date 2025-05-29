package com.demo.place.service.impl;

import com.demo.place.entity.DayOpening;
import com.demo.place.records.GroupedOpeningDayRecord;
import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.repository.PlaceRepository;
import com.demo.place.service.GroupPlaceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPlaceServiceImpl implements GroupPlaceService {

    private final List<DayOfWeek> dayOrder;
    private final PlaceRepository repository;

    @Override
    public GroupedPlaceRecord getGroupedOpeningHoursByPlaceId(Long id) {
        var place = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found: " + id));

        var byDay = place.getDays().stream().collect(Collectors.groupingBy(DayOpening::getDayOfWeek));

        LinkedHashMap<String, List<DayOfWeek>> groupedDays = new LinkedHashMap<>();
        Map<String, List<String>> intervalMap = new HashMap<>();

        for (DayOfWeek day : this.dayOrder) {
            var opens = byDay.getOrDefault(day, Collections.emptyList());
            List<String> intervals;

            if (opens.isEmpty()) {
                intervals = List.of("closed");
            } else {
                intervals = opens.stream()
                        .sorted(Comparator.comparing(DayOpening::getStartTime))
                        .map(o -> o.getStartTime() + " - " + o.getEndTime())
                        .collect(Collectors.toList());
            }

            var key = String.join(", ", intervals);
            groupedDays.computeIfAbsent(key, k -> new ArrayList<>()).add(day);
            intervalMap.putIfAbsent(key, intervals);
        }

        var openingGroups = groupedDays.entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> this.dayOrder.indexOf(e.getValue().getFirst())))
                .map(e -> new GroupedOpeningDayRecord(
                        formatDays(e.getValue()),
                        intervalMap.get(e.getKey())
                ))
                .collect(Collectors.toList());

        return new GroupedPlaceRecord(place.getId(),
                place.getLabel(),
                place.getLocation(),
                openingGroups);
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
