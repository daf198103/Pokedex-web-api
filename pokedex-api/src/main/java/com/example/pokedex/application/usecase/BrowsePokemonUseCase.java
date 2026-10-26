package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.PokemonSummary;

/** User Story 01: paginated Pokemon enumeration. */
public class BrowsePokemonUseCase {
    public static final int MAX_PAGE_SIZE = 50;

    private final PokemonCatalogPort catalog;

    public BrowsePokemonUseCase(PokemonCatalogPort catalog) {
        this.catalog = catalog;
    }

    public PagedResult<PokemonSummary> execute(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return catalog.list(page, size);
    }
}
