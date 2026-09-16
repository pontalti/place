package com.demo.place.records;

import java.util.List;

/**
 * Page envelope for the REST layer.
 *
 * <p>Written by hand because Quarkus has no equivalent of Spring Data's Page,
 * and because serialising a framework type would leak its internals into the
 * contract. The field names mirror the Spring branch so both implementations
 * of this challenge answer with the same shape.
 */
public record PageResponse<T>(
        List<T> content,
        int number,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static <T> PageResponse<T> of(List<T> content, int number, int size, long totalElements) {
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(
                content,
                number,
                size,
                totalElements,
                totalPages,
                number == 0,
                number >= totalPages - 1);
    }

    public static <T> PageResponse<T> empty(int number, int size, long totalElements) {
        return of(List.of(), number, size, totalElements);
    }
}