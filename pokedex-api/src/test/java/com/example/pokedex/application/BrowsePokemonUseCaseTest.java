package com.example.pokedex.application;

import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.application.usecase.BrowsePokemonUseCase;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.PagedResult;
import com.example.pokedex.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowsePokemonUseCaseTest {
    @Mock PokemonCatalogPort catalog;
    @InjectMocks BrowsePokemonUseCase useCase;

    @Test
    void rejectsNegativePage() {
        assertThrows(ValidationException.class, () -> useCase.execute(-1, 20));
    }

    @Test
    void rejectsInvalidSize() {
        assertThrows(ValidationException.class, () -> useCase.execute(0, 0));
        assertThrows(ValidationException.class, () -> useCase.execute(0, BrowsePokemonUseCase.MAX_PAGE_SIZE + 1));
    }

    @Test
    void delegatesToCatalog() {
        var expected = new PagedResult<PokemonSummary>(List.of(TestData.pikachuSummary()), 0, 20, 1);
        when(catalog.list(0, 20)).thenReturn(expected);

        assertSame(expected, useCase.execute(0, 20));
        assertEquals(1, expected.totalPages());
    }
}
