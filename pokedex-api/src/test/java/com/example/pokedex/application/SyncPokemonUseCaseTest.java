package com.example.pokedex.application;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.application.usecase.SyncPokemonUseCase;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.Pokemon;
import com.example.pokedex.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncPokemonUseCaseTest {
    @Mock PokemonCatalogPort catalog;
    @Mock LocalPokemonRepository repository;
    @InjectMocks SyncPokemonUseCase useCase;

    @Test
    void createsNewLocalPokemonWithoutCustomFields() {
        when(catalog.getSummary("25")).thenReturn(TestData.pikachuSummary());
        when(repository.findById(25)).thenReturn(Optional.empty());
        when(repository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        Pokemon result = useCase.execute(" 25 ");

        assertEquals("pikachu", result.name());
        assertNull(result.localizedName());
        assertEquals(List.of(), result.tags());
    }

    @Test
    void resyncKeepsCustomFieldsAndRefreshesRemoteOnes() {
        PokemonSummary updatedRemote = new PokemonSummary(25, "pikachu", "new.png", "Mouse Pokémon", 61, 4, List.of("static"));
        Pokemon existing = TestData.localPikachu("Pikachu BR", "Kanto", List.of("electric"), 3L);
        when(catalog.getSummary("pikachu")).thenReturn(updatedRemote);
        when(repository.findById(25)).thenReturn(Optional.of(existing));
        when(repository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("Pikachu");

        ArgumentCaptor<Pokemon> saved = ArgumentCaptor.forClass(Pokemon.class);
        verify(repository).save(saved.capture());
        assertEquals(61, saved.getValue().weight());
        assertEquals("new.png", saved.getValue().spriteUrl());
        assertEquals("Pikachu BR", saved.getValue().localizedName());
        assertEquals("Kanto", saved.getValue().region());
        assertEquals(List.of("electric"), saved.getValue().tags());
        assertEquals(3L, saved.getValue().version());
    }

    @Test
    void rejectsBlankIdentifier() {
        assertThrows(ValidationException.class, () -> useCase.execute("  "));
    }
}
