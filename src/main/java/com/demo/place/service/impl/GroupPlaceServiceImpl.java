package com.demo.place.service.impl;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.demo.place.annotation.DayOrder;
import com.demo.place.annotation.Log;
import com.demo.place.entity.DayOpening;
import com.demo.place.entity.Place;
import com.demo.place.records.GroupedOpeningDayRecord;
import com.demo.place.records.GroupedPlaceRecord;
import com.demo.place.repository.PlaceReadOnlyRepository;
import com.demo.place.service.GroupPlaceService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

/**
 * Condenses a place's opening hours into the weekly view: days of the week
 * that share exactly the same intervals are reported as one group,
 * such as {@code "Monday - Friday": ["11:30 - 15:00", "18:30 - 00:00"]}.
 *
 * <p>Read-only by nature, so it depends on {@link PlaceReadOnlyRepository} alone
 * and needs no transaction: the fetch graph loads the days together with the
 * place, and the grouping runs on data already in memory.
 */
@ApplicationScoped
public class GroupPlaceServiceImpl implements GroupPlaceService {

    /** Rendered for a day with no opening slot. */
    private static final String CLOSED = "closed";

    /** Day first, then start time — the order the intervals are listed in. */
    private static final Comparator<DayOpening> BY_DAY_THEN_START =
            Comparator.comparing(DayOpening::getDayOfWeek).thenComparing(DayOpening::getStartTime);

    /** Canonical week order; also the order the groups come back in. */
    private final List<DayOfWeek> dayOrder;

    private final PlaceReadOnlyRepository readOnlyRepository;

    /**
     * @param readOnlyRepository source of the place and its opening hours
     * @param dayOrder           the week order, produced by the {@link DayOrder}
     *                           qualified bean
     */
    @Inject
    public GroupPlaceServiceImpl(PlaceReadOnlyRepository readOnlyRepository,
                                 @DayOrder List<DayOfWeek> dayOrder) {
        this.readOnlyRepository = readOnlyRepository;
        this.dayOrder = dayOrder;
    }

    /**
     * Returns the place's identity plus its opening hours grouped by identical
     * schedules.
     *
     * <p>Grouping is by value, not by adjacency: if Monday and Wednesday share
     * intervals that Tuesday does not, they land in the same group, which is
     * then labelled by its first and last day. Days without any slot are
     * grouped together under {@value #CLOSED}.
     *
     * @param id the place id
     * @return the grouped view, ordered by the first day of each group
     * @throws WebApplicationException with status 404 when no place has that id
     */
    @Log
    @Override
    public GroupedPlaceRecord getGroupedOpeningHoursByPlaceId(Long id) {
        var place = this.readOnlyRepository.findById(id)
                .orElseThrow(() -> new WebApplicationException(
                        "Place not found: " + id, Response.Status.NOT_FOUND));

        /*
         * The fetch graph already brought the days along with the place, so the
         * dedicated interval query this method used to run was a second round
         * trip for data that was sitting right here.
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
     *
     * @param place a place whose days were loaded by the fetch graph
     * @return every day of the week mapped to its intervals as
     *         {@code "HH:mm - HH:mm"}; an empty list for a day without slots
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
     *
     * @param groups identical interval lists mapped to the days that share them
     * @return one record per group, ordered by the first day of each group
     */
    private List<GroupedOpeningDayRecord> buildOpeningGroups(
            Map<List<String>, List<DayOfWeek>> groups) {

        return groups.entrySet().stream()
                .sorted(Comparator.comparingInt(
                        entry -> this.dayOrder.indexOf(entry.getValue().getFirst())))
                .map(entry -> new GroupedOpeningDayRecord(
                        formatDays(entry.getValue()), entry.getKey()))
                .toList();
    }

    /**
     * Renders a day as a capitalised English name, e.g. {@code MONDAY} as
     * {@code "Monday"}.
     *
     * <p>{@link Locale#ROOT} keeps the result independent of the JVM's default
     * locale — under a Turkish locale, for instance, a plain
     * {@code toLowerCase()} turns the {@code I} of FRIDAY into a dotless ı.
     *
     * @param day the day to render
     * @return the capitalised name
     */
    protected String formatDay(DayOfWeek day) {
        var name = day.name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Renders a group's label: the single day, or {@code "First - Last"} when
     * the group spans more than one.
     *
     * <p>The label names only the first and last day. With grouping by value a
     * group can skip days — Monday and Wednesday without Tuesday — and the
     * label still reads {@code "Monday - Wednesday"}; this matches the output
     * the Spring branch produces.
     *
     * @param days the days of one group, in week order; never empty
     * @return the label for the group
     */
    protected String formatDays(List<DayOfWeek> days) {
        if (days.size() == 1) {
            return formatDay(days.getFirst());
        }
        return formatDay(days.getFirst()) + " - " + formatDay(days.getLast());
    }
}