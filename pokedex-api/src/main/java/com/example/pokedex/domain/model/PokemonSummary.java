package com.example.pokedex.domain.model;

import java.util.List;

/** Lightweight Pokemon data as read from the external catalog (PokeAPI). */
public record PokemonSummary(int id, String name, String spriteUrl, String category,
                             int weight, int height, List<String> abilities) {
    public PokemonSummary {
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
    }
}
