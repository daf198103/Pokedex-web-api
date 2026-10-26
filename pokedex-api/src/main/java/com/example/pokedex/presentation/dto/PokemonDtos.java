package com.example.pokedex.presentation.dto;

import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.Pokemon;
import com.example.pokedex.domain.model.PokemonDetail;
import com.example.pokedex.domain.model.PokemonSummary;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public final class PokemonDtos {
    private PokemonDtos() { }

    public record PageResponse<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
        public static <S, T> PageResponse<T> from(PagedResult<S> page, Function<S, T> mapper) {
            return new PageResponse<>(page.items().stream().map(mapper).toList(),
                    page.page(), page.size(), page.totalElements(), page.totalPages());
        }
    }

    /** weight is expressed in hectograms (PokeAPI unit). */
    public record SummaryResponse(int id, String name, String sprite, String category, int weight,
                                  List<String> abilities) {
        public static SummaryResponse from(PokemonSummary s) {
            return new SummaryResponse(s.id(), s.name(), s.spriteUrl(), s.category(), s.weight(), s.abilities());
        }
    }

    public record StatResponse(String name, int value) { }

    public record EvolutionResponse(int id, String name, List<EvolutionResponse> evolvesTo) {
        static EvolutionResponse from(PokemonDetail.EvolutionNode n) {
            return n == null ? null : new EvolutionResponse(n.id(), n.name(),
                    n.evolvesTo().stream().map(EvolutionResponse::from).toList());
        }
    }

    public record DetailResponse(int id, String name, String image, String category, int weight, int height,
                                 List<String> abilities, List<StatResponse> stats, String description,
                                 EvolutionResponse evolutionChain) {
        public static DetailResponse from(PokemonDetail d) {
            return new DetailResponse(d.id(), d.name(), d.imageUrl(), d.category(), d.weight(), d.height(),
                    d.abilities(), d.stats().stream().map(s -> new StatResponse(s.name(), s.value())).toList(),
                    d.description(), EvolutionResponse.from(d.evolutionChain()));
        }
    }

    public record LocalResponse(int id, String name, String sprite, String category, int weight, int height,
                                List<String> abilities, String localizedName, String region, List<String> tags,
                                Long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        public static LocalResponse from(Pokemon p) {
            return new LocalResponse(p.id(), p.name(), p.spriteUrl(), p.category(), p.weight(), p.height(),
                    p.abilities(), p.localizedName(), p.region(), p.tags(), p.version(), p.createdAt(), p.updatedAt());
        }
    }

    /** PUT replaces the proprietary fields. Send "version" to enable optimistic-lock checking. */
    public record UpdateLocalRequest(
            @Size(max = 100) String localizedName,
            @Size(max = 100) String region,
            @Size(max = 10) List<@NotBlank @Size(max = 30) String> tags,
            Long version) { }
}
