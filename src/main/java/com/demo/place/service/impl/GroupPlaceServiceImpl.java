package com.demo.place.service.impl;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.demo.place.records.DayIntervalRecord;
import com.demo.place.records.GroupedOpeningDayRecord;
import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.repository.PlaceRepository;
import com.demo.place.service.GroupPlaceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupPlaceServiceImpl implements GroupPlaceService {

	private final List<DayOfWeek> dayOrder;
	private final PlaceRepository repository;

	@Override
	public GroupedPlaceRecord getGroupedOpeningHoursByPlaceId(Long id) {
		var place = this.repository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found: " + id));

		var openings = this.repository.findGroupedOpeningsByPlaceId(id);

		Map<DayOfWeek, List<String>> byDay = buildMapByDay(openings);

		LinkedHashMap<String, List<DayOfWeek>> groupedDays = new LinkedHashMap<>();
		Map<String, List<String>> intervalMap = new HashMap<>();
		
		this.dayOrder.forEach(day->{
			List<String> intervals = byDay.getOrDefault(day, Collections.emptyList());
			if (intervals.isEmpty()) {
				intervals = List.of("closed");
			}

			var key = String.join(", ", intervals);
			groupedDays.computeIfAbsent(key, k -> new ArrayList<>()).add(day);
			intervalMap.putIfAbsent(key, intervals);			
		});

		var openingGroups = buildOpeningGroups(groupedDays, intervalMap);

		return new GroupedPlaceRecord(place.getId(), place.getLabel(), place.getLocation(), openingGroups);
	}

	private Map<DayOfWeek, List<String>> buildMapByDay(List<DayIntervalRecord> openings) {
		Map<DayOfWeek, List<String>> byDay = new HashMap<>();
		this.dayOrder.forEach(day->byDay.put(day, new ArrayList<>()));
		openings.forEach(record->  byDay.get(record.day()).add(record.startTime() + " - " + record.endTime()));
		return byDay;
	}
	
	private List<GroupedOpeningDayRecord> buildOpeningGroups(LinkedHashMap<String, List<DayOfWeek>> groupedDays,
			Map<String, List<String>> intervalMap) {
		return groupedDays.entrySet().stream()
				.sorted(Comparator.comparing(e -> this.dayOrder.indexOf(e.getValue().getFirst())))
				.map(e -> new GroupedOpeningDayRecord(formatDays(e.getValue()), intervalMap.get(e.getKey())))
				.collect(Collectors.toList());
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
