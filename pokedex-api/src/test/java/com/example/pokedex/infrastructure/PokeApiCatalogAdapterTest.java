package com.example.pokedex.infrastructure;

import com.example.pokedex.domain.exception.NotFoundException;
import com.example.pokedex.domain.exception.UpstreamException;
import com.example.pokedex.domain.model.PokemonDetail;
import com.example.pokedex.infrastructure.pokeapi.PokeApiCatalogAdapter;
import com.example.pokedex.infrastructure.pokeapi.PokeApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PokeApiCatalogAdapterTest {
    private static final String BASE = "https://pokeapi.co/api/v2";

    private MockRestServiceServer server;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).ignoreExpectOrder(true).build();
        adapter = new PokeApiCatalogAdapter(new PokeApiClient(builder.build()));
    }

    private static final String EEVEE = """
            {"id":133,"name":"eevee","weight":65,"height":3,
             "abilities":[{"ability":{"name":"run-away","url":"x"}}],
             "stats":[{"base_stat":55,"stat":{"name":"hp","url":"x"}}],
             "sprites":{"front_default":"front.png","other":{"official-artwork":{"front_default":"art.png"}}},
             "species":{"name":"eevee","url":"https://pokeapi.co/api/v2/pokemon-species/133/"}}
            """;

    private static final String SPECIES = """
            {"genera":[{"genus":"Evolution Pokémon","language":{"name":"en"}}],
             "flavor_text_entries":[{"flavor_text":"Line one.\\nLine two.\\f","language":{"name":"en"}}],
             "evolution_chain":{"url":"https://pokeapi.co/api/v2/evolution-chain/67/"}}
            """;

    private static final String CHAIN = """
            {"chain":{"species":{"name":"eevee","url":"https://pokeapi.co/api/v2/pokemon-species/133/"},
              "evolves_to":[
                {"species":{"name":"vaporeon","url":"https://pokeapi.co/api/v2/pokemon-species/134/"},"evolves_to":[]},
                {"species":{"name":"jolteon","url":"https://pokeapi.co/api/v2/pokemon-species/135/"},"evolves_to":[]}]}}
            """;

    @Test
    void buildsDetailWithBranchingEvolutionAndCleanDescription() {
        server.expect(requestTo(BASE + "/pokemon/eevee")).andRespond(withSuccess(EEVEE, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/pokemon-species/133")).andRespond(withSuccess(SPECIES, MediaType.APPLICATION_JSON));
        server.expect(requestTo(BASE + "/evolution-chain/67")).andRespond(withSuccess(CHAIN, MediaType.APPLICATION_JSON));

        PokemonDetail detail = adapter.getDetail("Eevee");

        assertEquals(133, detail.id());
        assertEquals("art.png", detail.imageUrl());
        assertEquals("Evolution Pokémon", detail.category());
        assertEquals("Line one. Line two.", detail.description());
        assertEquals("hp", detail.stats().get(0).name());
        assertEquals(2, detail.evolutionChain().evolvesTo().size());
        assertEquals("jolteon", detail.evolutionChain().evolvesTo().get(1).name());
        server.verify();
    }

    @Test
    void mapsUpstream404ToNotFound() {
        server.expect(requestTo(BASE + "/pokemon/missingno")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(NotFoundException.class, () -> adapter.getSummary("missingno"));
    }

    @Test
    void mapsUpstream5xxToUpstreamException() {
        server.expect(requestTo(BASE + "/pokemon/pikachu")).andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));
        assertThrows(UpstreamException.class, () -> adapter.getSummary("pikachu"));
    }
}
