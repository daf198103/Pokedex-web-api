package com.example.pokedex.domain.model;

import java.time.LocalDateTime;
import java.util.List;

/** A Pokemon replicated in the local store, enriched with proprietary fields. */
public record Pokemon(int id, String name, String spriteUrl, String category, int weight, int height,
                      List<String> abilities,
                      String localizedName, String region, List<String> tags,
                      Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {

    public Pokemon {
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
        tags = tags == null ? List.of() : List.copyOf(tags);
    }

    public static Pokemon fromSummary(PokemonSummary s) {
        return new Pokemon(s.id(), s.name(), s.spriteUrl(), s.category(), s.weight(), s.height(),
                s.abilities(), null, null, List.of(), null, null, null);
    }

    /** Refreshes PokeAPI-owned fields while keeping every proprietary field untouched. */
    public Pokemon refreshedFrom(PokemonSummary s) {
        return new Pokemon(id, s.name(), s.spriteUrl(), s.category(), s.weight(), s.height(),
                s.abilities(), localizedName, region, tags, version, createdAt, updatedAt);
    }

    public Pokemon withCustomFields(String localizedName, String region, List<String> tags) {
        return new Pokemon(id, name, spriteUrl, category, weight, height, abilities,
                localizedName, region, tags, version, createdAt, updatedAt);
    }
}
