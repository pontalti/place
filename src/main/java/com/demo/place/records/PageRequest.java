package com.demo.place.records;

import java.util.List;
import java.util.Locale;

/**
 * Normalised paging parameters.
 *
 * <p>The sort property is checked against an allow-list: it ends up inside an
 * ORDER BY clause that is built as a string, so accepting it verbatim would be
 * an injection point. Bind parameters cannot be used for identifiers.
 */
public record PageRequest(int number, int size, String property, boolean ascending) {

    private static final List<String> SORTABLE = List.of("id", "label", "location");

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_PROPERTY = "label";

    public static PageRequest of(Integer page, Integer size, String sort) {
        int resolvedNumber = page == null || page < 0 ? 0 : page;
        int resolvedSize = size == null || size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);

        String property = DEFAULT_PROPERTY;
        boolean ascending = true;

        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            String candidate = parts[0].trim().toLowerCase(Locale.ROOT);
            if (SORTABLE.contains(candidate)) {
                property = candidate;
            }
            if (parts.length > 1) {
                ascending = !"desc".equalsIgnoreCase(parts[1].trim());
            }
        }

        return new PageRequest(resolvedNumber, resolvedSize, property, ascending);
    }

    /** Safe to interpolate: the property came from the allow-list above. */
    public String orderByClause(String alias) {
        return " ORDER BY " + alias + "." + this.property + (this.ascending ? " ASC" : " DESC");
    }

    public int offset() {
        return this.number * this.size;
    }
}