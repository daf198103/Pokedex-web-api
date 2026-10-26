package com.example.pokedex.infrastructure.pokeapi;

import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.PokemonDetail;
import com.example.pokedex.domain.model.PokemonDetail.EvolutionNode;
import com.example.pokedex.domain.model.PokemonDetail.Stat;
import com.example.pokedex.domain.model.PokemonSummary;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.EvolutionLink;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.PokemonDto;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.PokemonList;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.SpeciesDto;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Adapter: translates PokeAPI payloads into domain models. */
@Component
public class PokeApiCatalogAdapter implements PokemonCatalogPort {
    private final PokeApiClient client;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public PokeApiCatalogAdapter(PokeApiClient client) {
        this.client = client;
    }

    @PreDestroy
    void shutdown() {
        executor.shutdown();
    }

    @Override
    public PagedResult<PokemonSummary> list(int page, int size) {
        int offset = (int) Math.min((long) page * size, Integer.MAX_VALUE);
        PokemonList listing = client.getPokemonPage(size, offset);

        // The list endpoint only returns names + urls, so fetch each summary concurrently (cached).
        List<CompletableFuture<PokemonSummary>> futures = listing.results().stream()
                .map(r -> CompletableFuture.supplyAsync(() -> getSummary(r.name()), executor))
                .toList();
        List<PokemonSummary> items = futures.stream().map(PokeApiCatalogAdapter::join).toList();
        return new PagedResult<>(items, page, size, listing.count());
    }

    @Override
    public PokemonSummary getSummary(String idOrName) {
        PokemonDto pokemon = client.getPokemon(normalize(idOrName));
        SpeciesDto species = client.getSpecies(idFromUrl(pokemon.species().url()));
        return toSummary(pokemon, species);
    }

    @Override
    public PokemonDetail getDetail(String idOrName) {
        PokemonDto pokemon = client.getPokemon(normalize(idOrName));
        SpeciesDto species = client.getSpecies(idFromUrl(pokemon.species().url()));
        PokemonSummary summary = toSummary(pokemon, species);

        EvolutionNode evolution = null;
        if (species.evolutionChain() != null && species.evolutionChain().url() != null) {
            var chain = client.getEvolutionChain(idFromUrl(species.evolutionChain().url()));
            evolution = toNode(chain.chain());
        }

        List<Stat> stats = pokemon.stats() == null ? List.of() : pokemon.stats().stream()
                .map(s -> new Stat(s.stat().name(), s.baseStat()))
                .toList();

        return new PokemonDetail(summary.id(), summary.name(), imageUrl(pokemon), summary.category(),
                summary.weight(), summary.height(), summary.abilities(), stats, description(species), evolution);
    }

    // ---- mapping helpers ----

    private static PokemonSummary toSummary(PokemonDto p, SpeciesDto species) {
        String sprite = p.sprites() == null ? null : p.sprites().frontDefault();
        List<String> abilities = p.abilities() == null ? List.of() : p.abilities().stream()
                .map(a -> a.ability().name())
                .toList();
        return new PokemonSummary(p.id(), p.name(), sprite, category(species), p.weight(), p.height(), abilities);
    }

    private static String imageUrl(PokemonDto p) {
        if (p.sprites() == null) {
            return null;
        }
        if (p.sprites().other() != null && p.sprites().other().officialArtwork() != null
                && p.sprites().other().officialArtwork().frontDefault() != null) {
            return p.sprites().other().officialArtwork().frontDefault();
        }
        return p.sprites().frontDefault();
    }

    private static String category(SpeciesDto species) {
        if (species.genera() == null) {
            return null;
        }
        return species.genera().stream()
                .filter(g -> g.language() != null && "en".equals(g.language().name()))
                .map(PokeApiDtos.Genus::genus)
                .findFirst()
                .orElse(null);
    }

    private static String description(SpeciesDto species) {
        if (species.flavorTextEntries() == null) {
            return null;
        }
        return species.flavorTextEntries().stream()
                .filter(f -> f.language() != null && "en".equals(f.language().name()))
                .map(f -> f.flavorText().replaceAll("\\s+", " ").trim())
                .findFirst()
                .orElse(null);
    }

    private static EvolutionNode toNode(EvolutionLink link) {
        List<EvolutionNode> children = link.evolvesTo() == null ? List.of() : link.evolvesTo().stream()
                .map(PokeApiCatalogAdapter::toNode)
                .toList();
        return new EvolutionNode(idFromUrl(link.species().url()), link.species().name(), children);
    }

    static int idFromUrl(String url) {
        String trimmed = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
        return Integer.parseInt(trimmed.substring(trimmed.lastIndexOf('/') + 1));
    }

    private static String normalize(String idOrName) {
        return idOrName.trim().toLowerCase(Locale.ROOT);
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException re) {
                throw re;
            }
            throw e;
        }
    }
}
