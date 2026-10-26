package com.example.pokedex.domain.model;

import java.util.List;

public record PokemonDetail(int id, String name, String imageUrl, String category, int weight, int height,
                            List<String> abilities, List<Stat> stats, String description,
                            EvolutionNode evolutionChain) {

    public record Stat(String name, int value) { }

    /** Recursive tree: supports branching evolutions such as Eevee. */
    public record EvolutionNode(int id, String name, List<EvolutionNode> evolvesTo) { }
}
