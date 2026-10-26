package com.example.pokedex.presentation.controller;

import com.example.pokedex.application.usecase.BrowsePokemonUseCase;
import com.example.pokedex.application.usecase.GetPokemonDetailUseCase;
import com.example.pokedex.application.usecase.SyncPokemonUseCase;
import com.example.pokedex.presentation.dto.PokemonDtos.DetailResponse;
import com.example.pokedex.presentation.dto.PokemonDtos.LocalResponse;
import com.example.pokedex.presentation.dto.PokemonDtos.PageResponse;
import com.example.pokedex.presentation.dto.PokemonDtos.SummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Remote catalog (PokeAPI-backed). GET endpoints are public, sync requires authentication. */
@RestController
@RequestMapping("/api/pokemon")
public class PokemonController {
    private final BrowsePokemonUseCase browse;
    private final GetPokemonDetailUseCase detail;
    private final SyncPokemonUseCase sync;

    public PokemonController(BrowsePokemonUseCase browse, GetPokemonDetailUseCase detail, SyncPokemonUseCase sync) {
        this.browse = browse;
        this.detail = detail;
        this.sync = sync;
    }

    @GetMapping
    public PageResponse<SummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(browse.execute(page, size), SummaryResponse::from);
    }

    @GetMapping("/{idOrName}")
    public DetailResponse get(@PathVariable String idOrName) {
        return DetailResponse.from(detail.execute(idOrName));
    }

    @PostMapping("/{idOrName}/sync")
    public LocalResponse sync(@PathVariable String idOrName) {
        return LocalResponse.from(sync.execute(idOrName));
    }
}
