package com.example.pokedex.application.port;

import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.PokemonDetail;
import com.example.pokedex.domain.model.PokemonSummary;

/** Outbound port: read-only access to the external Pokemon catalog. */
public interface PokemonCatalogPort {
    PagedResult<PokemonSummary> list(int page, int size);

    PokemonSummary getSummary(String idOrName);

    PokemonDetail getDetail(String idOrName);
}
