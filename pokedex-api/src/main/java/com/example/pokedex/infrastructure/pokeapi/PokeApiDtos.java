package com.example.pokedex.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** Raw PokeAPI payloads. These never leave the infrastructure layer. */
public final class PokeApiDtos {
    private PokeApiDtos() { }

    public record NamedResource(String name, String url) { }

    public record PokemonList(int count, List<NamedResource> results) { }

    public record PokemonDto(int id, String name, int weight, int height,
                             List<AbilitySlot> abilities, List<StatSlot> stats,
                             Sprites sprites, NamedResource species) { }

    public record AbilitySlot(NamedResource ability) { }

    public record StatSlot(@JsonProperty("base_stat") int baseStat, NamedResource stat) { }

    public record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) { }

    public record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork) { }

    public record Artwork(@JsonProperty("front_default") String frontDefault) { }

    public record SpeciesDto(List<Genus> genera,
                             @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
                             @JsonProperty("evolution_chain") UrlResource evolutionChain) { }

    public record Genus(String genus, NamedResource language) { }

    public record FlavorText(@JsonProperty("flavor_text") String flavorText, NamedResource language) { }

    public record UrlResource(String url) { }

    public record EvolutionChainDto(EvolutionLink chain) { }

    public record EvolutionLink(NamedResource species, @JsonProperty("evolves_to") List<EvolutionLink> evolvesTo) { }
}
