package com.example.pokedex.infrastructure.config;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.application.port.PasswordHasher;
import com.example.pokedex.application.port.PokemonCatalogPort;
import com.example.pokedex.application.port.TokenIssuer;
import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.application.usecase.BrowsePokemonUseCase;
import com.example.pokedex.application.usecase.GetPokemonDetailUseCase;
import com.example.pokedex.application.usecase.LoginUseCase;
import com.example.pokedex.application.usecase.ManageLocalPokemonUseCase;
import com.example.pokedex.application.usecase.RegisterUserUseCase;
import com.example.pokedex.application.usecase.SyncPokemonUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires framework-free use cases so the application layer has no Spring dependency. */
@Configuration
public class UseCaseConfig {

    @Bean
    BrowsePokemonUseCase browsePokemonUseCase(PokemonCatalogPort catalog) {
        return new BrowsePokemonUseCase(catalog);
    }

    @Bean
    GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonCatalogPort catalog) {
        return new GetPokemonDetailUseCase(catalog);
    }

    @Bean
    SyncPokemonUseCase syncPokemonUseCase(PokemonCatalogPort catalog, LocalPokemonRepository repository) {
        return new SyncPokemonUseCase(catalog, repository);
    }

    @Bean
    ManageLocalPokemonUseCase manageLocalPokemonUseCase(LocalPokemonRepository repository) {
        return new ManageLocalPokemonUseCase(repository);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher hasher) {
        return new RegisterUserUseCase(users, hasher);
    }

    @Bean
    LoginUseCase loginUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        return new LoginUseCase(users, hasher, tokens);
    }
}
