package com.example.pokedex.application.port;

import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.Pokemon;

import java.util.Optional;

/** Outbound port: persistence of locally replicated Pokemon. */
public interface LocalPokemonRepository {
    Pokemon save(Pokemon pokemon);

    Optional<Pokemon> findById(int id);

    PagedResult<Pokemon> findAll(int page, int size);

    boolean existsById(int id);

    void deleteById(int id);
}
