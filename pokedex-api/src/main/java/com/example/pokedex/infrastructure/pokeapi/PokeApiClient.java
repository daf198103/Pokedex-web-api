package com.example.pokedex.infrastructure.pokeapi;

import com.example.pokedex.domain.exception.NotFoundException;
import com.example.pokedex.domain.exception.UpstreamException;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.EvolutionChainDto;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.PokemonDto;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.PokemonList;
import com.example.pokedex.infrastructure.pokeapi.PokeApiDtos.SpeciesDto;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Thin HTTP client with caching. Kept separate from the adapter so @Cacheable proxies apply. */
@Component
public class PokeApiClient {
    private final RestClient rest;

    public PokeApiClient(RestClient pokeApiRestClient) {
        this.rest = pokeApiRestClient;
    }

    @Cacheable("pokeapi-page")
    public PokemonList getPokemonPage(int limit, int offset) {
        return fetch(PokemonList.class, "/pokemon?limit={limit}&offset={offset}", limit, offset);
    }

    @Cacheable("pokeapi-pokemon")
    public PokemonDto getPokemon(String idOrName) {
        return fetch(PokemonDto.class, "/pokemon/{idOrName}", idOrName);
    }

    @Cacheable("pokeapi-species")
    public SpeciesDto getSpecies(int id) {
        return fetch(SpeciesDto.class, "/pokemon-species/{id}", id);
    }

    @Cacheable("pokeapi-evolution")
    public EvolutionChainDto getEvolutionChain(int id) {
        return fetch(EvolutionChainDto.class, "/evolution-chain/{id}", id);
    }

    private <T> T fetch(Class<T> type, String uri, Object... vars) {
        try {
            T body = rest.get().uri(uri, vars).retrieve().body(type);
            if (body == null) {
                throw new UpstreamException("PokeAPI returned an empty response");
            }
            return body;
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Pokemon resource not found in PokeAPI");
        } catch (RestClientException e) {
            throw new UpstreamException("PokeAPI is unavailable: " + e.getMessage());
        }
    }
}
