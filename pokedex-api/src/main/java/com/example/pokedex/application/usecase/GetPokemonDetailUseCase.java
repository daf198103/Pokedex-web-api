package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.PokemonDetail;

/** User Story 02: detailed view. */
public class GetPokemonDetailUseCase {
    private final PokemonCatalogPort catalog;

    public GetPokemonDetailUseCase(PokemonCatalogPort catalog) {
        this.catalog = catalog;
    }

    public PokemonDetail execute(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            throw new ValidationException("Pokemon id or name is required");
        }
        return catalog.getDetail(idOrName.trim().toLowerCase());
    }
}
