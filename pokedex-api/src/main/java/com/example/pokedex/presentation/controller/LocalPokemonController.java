package com.example.pokedex.presentation.controller;

import com.example.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.example.pokedex.application.usecase.ManageLocalPokemonUseCase.UpdateCommand;
import com.example.pokedex.presentation.dto.PokemonDtos.LocalResponse;
import com.example.pokedex.presentation.dto.PokemonDtos.PageResponse;
import com.example.pokedex.presentation.dto.PokemonDtos.UpdateLocalRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Locally replicated Pokemon. All routes require authentication. */
@RestController
@RequestMapping("/api/local/pokemon")
public class LocalPokemonController {
    private final ManageLocalPokemonUseCase manage;

    public LocalPokemonController(ManageLocalPokemonUseCase manage) {
        this.manage = manage;
    }

    @GetMapping
    public PageResponse<LocalResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(manage.list(page, size), LocalResponse::from);
    }

    @GetMapping("/{id}")
    public LocalResponse get(@PathVariable int id) {
        return LocalResponse.from(manage.get(id));
    }

    @PutMapping("/{id}")
    public LocalResponse update(@PathVariable int id, @Valid @RequestBody UpdateLocalRequest request) {
        return LocalResponse.from(manage.update(
                new UpdateCommand(id, request.localizedName(), request.region(), request.tags(), request.version())));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int id) {
        manage.delete(id);
    }
}
