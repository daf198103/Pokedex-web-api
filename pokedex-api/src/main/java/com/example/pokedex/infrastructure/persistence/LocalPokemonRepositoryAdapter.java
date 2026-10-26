package com.example.pokedex.infrastructure.persistence;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.domain.exception.ConflictException;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.Pokemon;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class LocalPokemonRepositoryAdapter implements LocalPokemonRepository {
    private final PokemonJpaRepository jpa;

    public LocalPokemonRepositoryAdapter(PokemonJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Pokemon save(Pokemon p) {
        PokemonEntity entity = jpa.findById(p.id()).orElseGet(PokemonEntity::new);
        entity.setId(p.id());
        entity.setName(p.name());
        entity.setSpriteUrl(p.spriteUrl());
        entity.setCategory(p.category());
        entity.setWeight(p.weight());
        entity.setHeight(p.height());
        entity.setAbilities(p.abilities());
        entity.setLocalizedName(p.localizedName());
        entity.setRegion(p.region());
        entity.setTags(p.tags());
        try {
            return toDomain(jpa.saveAndFlush(entity));
        } catch (OptimisticLockingFailureException e) {
            throw new ConflictException("Pokemon " + p.id() + " was modified concurrently, please retry");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pokemon> findById(int id) {
        return jpa.findById(id).map(LocalPokemonRepositoryAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResult<Pokemon> findAll(int page, int size) {
        Page<PokemonEntity> result = jpa.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PagedResult<>(result.getContent().stream().map(LocalPokemonRepositoryAdapter::toDomain).toList(),
                page, size, result.getTotalElements());
    }

    @Override
    public boolean existsById(int id) {
        return jpa.existsById(id);
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        jpa.deleteById(id);
    }

    private static Pokemon toDomain(PokemonEntity e) {
        return new Pokemon(e.getId(), e.getName(), e.getSpriteUrl(), e.getCategory(), e.getWeight(), e.getHeight(),
                e.getAbilities(), e.getLocalizedName(), e.getRegion(), e.getTags(),
                e.getVersion(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
