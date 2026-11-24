package com.example.spacecatsmarket.domain;

import java.util.List;

/**
 * One page of domain objects. Lives in the domain layer so services can return pages
 * without depending on web DTOs.
 */
public record PageResult<T>(List<T> content, int page, int size, long totalElements) {

    public PageResult {
        content = List.copyOf(content);
    }

    public boolean hasNext() {
        return (long) (page + 1) * size < totalElements;
    }
}
