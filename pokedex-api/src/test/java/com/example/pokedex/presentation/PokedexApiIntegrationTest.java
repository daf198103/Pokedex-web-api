package com.example.pokedex.presentation;

import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.PokemonSummary;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.seed.enabled=false")
@AutoConfigureMockMvc
class PokedexApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @MockBean PokemonCatalogPort catalog;

    private static final PokemonSummary PIKACHU =
            new PokemonSummary(25, "pikachu", "pikachu.png", "Mouse Pokémon", 60, 4, List.of("static"));

    @Test
    void browseIsPublicAndPaginated() throws Exception {
        when(catalog.list(0, 20)).thenReturn(new PagedResult<>(List.of(PIKACHU), 0, 20, 1302));

        mvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("pikachu"))
                .andExpect(jsonPath("$.totalElements").value(1302))
                .andExpect(jsonPath("$.totalPages").value(66));
    }

    @Test
    void browseValidatesPagingParameters() throws Exception {
        mvc.perform(get("/api/pokemon?size=500")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/pokemon?page=abc")).andExpect(status().isBadRequest());
    }

    @Test
    void protectedRoutesRequireAuthentication() throws Exception {
        mvc.perform(post("/api/pokemon/25/sync")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/local/pokemon")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/local/pokemon").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registerLoginSyncUpdateDeleteFlow() throws Exception {
        String credentials = "{\"username\":\"ash\",\"email\":\"ash@example.com\",\"password\":\"pikachu123\"}";

        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isConflict());

        String loginBody = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"pikachu123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String auth = "Bearer " + mapper.readTree(loginBody).get("token").asText();

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());

        when(catalog.getSummary("25")).thenReturn(PIKACHU);
        mvc.perform(post("/api/pokemon/25/sync").header("Authorization", auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("pikachu"))
                .andExpect(jsonPath("$.version").value(0));

        mvc.perform(put("/api/local/pokemon/25").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localizedName\":\"Pikachu\",\"region\":\"Kanto\",\"tags\":[\"electric\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.region").value("Kanto"))
                .andExpect(jsonPath("$.tags[0]").value("electric"));

        // stale optimistic-lock version -> 409
        mvc.perform(put("/api/local/pokemon/25").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"region\":\"Johto\",\"version\":999}"))
                .andExpect(status().isConflict());

        // malformed payload -> 400, invalid constraint -> 400, missing record -> 404
        mvc.perform(put("/api/local/pokemon/25").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON).content("{bad json"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/local/pokemon/25").header("Authorization", auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tags\":[\"a\",\"b\",\"c\",\"d\",\"e\",\"f\",\"g\",\"h\",\"i\",\"j\",\"k\"]}"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/local/pokemon/999").header("Authorization", auth))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/local/pokemon/25").header("Authorization", auth))
                .andExpect(status().isNoContent());
        mvc.perform(delete("/api/local/pokemon/25").header("Authorization", auth))
                .andExpect(status().isNotFound());
    }
}
