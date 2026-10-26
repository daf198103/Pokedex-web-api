package com.example.pokedex.application;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.example.pokedex.application.usecase.ManageLocalPokemonUseCase.UpdateCommand;
import com.example.pokedex.domain.exception.ConflictException;
import com.example.pokedex.domain.exception.NotFoundException;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.Pokemon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManageLocalPokemonUseCaseTest {
    @Mock LocalPokemonRepository repository;
    @InjectMocks ManageLocalPokemonUseCase useCase;

    @Test
    void getThrowsNotFoundWhenMissing() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> useCase.get(99));
    }

    @Test
    void updateNormalizesAndPersistsCustomFields() {
        when(repository.findById(25)).thenReturn(Optional.of(TestData.localPikachu(null, null, List.of(), 1L)));
        when(repository.save(any(Pokemon.class))).thenAnswer(inv -> inv.getArgument(0));

        Pokemon result = useCase.update(new UpdateCommand(25, "  Pikachu  ", "   ",
                List.of(" electric ", "electric", "mascot"), 1L));

        assertEquals("Pikachu", result.localizedName());
        assertNull(result.region());
        assertEquals(List.of("electric", "mascot"), result.tags());
    }

    @Test
    void updateFailsOnVersionMismatch() {
        when(repository.findById(25)).thenReturn(Optional.of(TestData.localPikachu(null, null, List.of(), 5L)));
        assertThrows(ConflictException.class,
                () -> useCase.update(new UpdateCommand(25, "x", null, List.of(), 4L)));
        verify(repository, never()).save(any());
    }

    @Test
    void updateRejectsInvalidTags() {
        when(repository.findById(25)).thenReturn(Optional.of(TestData.localPikachu(null, null, List.of(), 1L)));

        assertThrows(ValidationException.class,
                () -> useCase.update(new UpdateCommand(25, null, null, List.of("a,b"), null)));

        List<String> tooMany = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            tooMany.add("tag" + i);
        }
        assertThrows(ValidationException.class,
                () -> useCase.update(new UpdateCommand(25, null, null, tooMany, null)));
    }

    @Test
    void deleteThrowsNotFoundWhenMissing() {
        when(repository.existsById(7)).thenReturn(false);
        assertThrows(NotFoundException.class, () -> useCase.delete(7));
        verify(repository, never()).deleteById(7);
    }

    @Test
    void deleteRemovesExisting() {
        when(repository.existsById(7)).thenReturn(true);
        useCase.delete(7);
        verify(repository).deleteById(7);
    }
}
