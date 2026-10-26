package com.example.pokedex.domain.model;

import java.util.List;

public record PagedResult<T>(List<T> items, int page, int size, long totalElements) {
    public int totalPages() {
        return size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);
    }
}
