package com.example.pokedex.application;

import com.example.pokedex.domain.model.Pokemon;
import com.example.pokedex.domain.model.PokemonSummary;

import java.time.LocalDateTime;
import java.util.List;

public final class TestData {
    private TestData() { }

    public static PokemonSummary pikachuSummary() {
        return new PokemonSummary(25, "pikachu", "pikachu.png", "Mouse Pokémon", 60, 4, List.of("static"));
    }

    public static Pokemon localPikachu(String localizedName, String region, List<String> tags, Long version) {
        return new Pokemon(25, "pikachu", "pikachu.png", "Mouse Pokémon", 60, 4, List.of("static"),
                localizedName, region, tags, version, LocalDateTime.now(), LocalDateTime.now());
    }
}
