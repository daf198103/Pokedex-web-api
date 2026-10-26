package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.Pokemon;
import com.example.pokedex.domain.model.PokemonSummary;

/** User Story 03: replicate a Pokemon locally (idempotent upsert, custom fields preserved). */
public class SyncPokemonUseCase {
    private final PokemonCatalogPort catalog;
    private final LocalPokemonRepository repository;

    public SyncPokemonUseCase(PokemonCatalogPort catalog, LocalPokemonRepository repository) {
        this.catalog = catalog;
        this.repository = repository;
    }

    public Pokemon execute(String idOrName) {
        if (idOrName == null || idOrName.isBlank()) {
            throw new ValidationException("Pokemon id or name is required");
        }
        PokemonSummary remote = catalog.getSummary(idOrName.trim().toLowerCase());
        Pokemon toSave = repository.findById(remote.id())
                .map(existing -> existing.refreshedFrom(remote))
                .orElseGet(() -> Pokemon.fromSummary(remote));
        return repository.save(toSave);
    }
}
