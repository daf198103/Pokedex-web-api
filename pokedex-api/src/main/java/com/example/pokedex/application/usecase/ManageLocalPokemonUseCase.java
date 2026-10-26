package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.domain.exception.ConflictException;
import com.example.pokedex.domain.exception.NotFoundException;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.Pokemon;

import java.util.LinkedHashSet;
import java.util.List;

/** User Story 04: read/update/delete Pokemon stored locally. */
public class ManageLocalPokemonUseCase {
    public static final int MAX_PAGE_SIZE = 100;
    static final int MAX_TEXT = 100;
    static final int MAX_TAGS = 10;
    static final int MAX_TAG_LENGTH = 30;

    private final LocalPokemonRepository repository;

    public ManageLocalPokemonUseCase(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    public record UpdateCommand(int id, String localizedName, String region, List<String> tags, Long expectedVersion) { }

    public PagedResult<Pokemon> list(int page, int size) {
        if (page < 0) {
            throw new ValidationException("page must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationException("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return repository.findAll(page, size);
    }

    public Pokemon get(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Pokemon " + id + " not found in local database"));
    }

    public Pokemon update(UpdateCommand cmd) {
        Pokemon existing = get(cmd.id());

        if (cmd.expectedVersion() != null && !cmd.expectedVersion().equals(existing.version())) {
            throw new ConflictException("Pokemon " + cmd.id() + " was modified by someone else (current version "
                    + existing.version() + ")");
        }

        String localizedName = normalizeText("localizedName", cmd.localizedName());
        String region = normalizeText("region", cmd.region());
        List<String> tags = normalizeTags(cmd.tags());

        return repository.save(existing.withCustomFields(localizedName, region, tags));
    }

    public void delete(int id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Pokemon " + id + " not found in local database");
        }
        repository.deleteById(id);
    }

    private static String normalizeText(String field, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() > MAX_TEXT) {
            throw new ValidationException(field + " must be at most " + MAX_TEXT + " characters");
        }
        return trimmed;
    }

    private static List<String> normalizeTags(List<String> tags) {
        if (tags == null) {
            return List.of();
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (String tag : tags) {
            if (tag == null || tag.isBlank()) {
                throw new ValidationException("tags must not contain blank values");
            }
            String t = tag.trim();
            if (t.length() > MAX_TAG_LENGTH) {
                throw new ValidationException("each tag must be at most " + MAX_TAG_LENGTH + " characters");
            }
            if (t.contains(",")) {
                throw new ValidationException("tags must not contain commas");
            }
            unique.add(t);
        }
        if (unique.size() > MAX_TAGS) {
            throw new ValidationException("at most " + MAX_TAGS + " tags are allowed");
        }
        return List.copyOf(unique);
    }
}
