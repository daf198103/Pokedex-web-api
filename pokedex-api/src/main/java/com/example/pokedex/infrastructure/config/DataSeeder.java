package com.example.pokedex.infrastructure.config;

import com.example.pokedex.application.port.LocalPokemonRepository;
import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.application.usecase.RegisterUserUseCase;
import com.example.pokedex.domain.model.Pokemon;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds a demo user (demo / demo1234) and a few Pokemon for demonstration purposes. */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {
    private static final String SPRITE = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";

    private final RegisterUserUseCase register;
    private final UserRepository users;
    private final LocalPokemonRepository pokemon;

    public DataSeeder(RegisterUserUseCase register, UserRepository users, LocalPokemonRepository pokemon) {
        this.register = register;
        this.users = users;
        this.pokemon = pokemon;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByUsername("demo")) {
            register.execute("demo", "demo@example.com", "demo1234");
        }
        if (pokemon.findAll(0, 1).totalElements() == 0) {
            pokemon.save(seed(1, "bulbasaur", "Seed Pokémon", 69, 7, List.of("overgrow", "chlorophyll"), "Bulbasaur"));
            pokemon.save(seed(4, "charmander", "Lizard Pokémon", 85, 6, List.of("blaze", "solar-power"), "Charmander"));
            pokemon.save(seed(7, "squirtle", "Tiny Turtle Pokémon", 90, 5, List.of("torrent", "rain-dish"), "Squirtle"));
            pokemon.save(seed(25, "pikachu", "Mouse Pokémon", 60, 4, List.of("static", "lightning-rod"), "Pikachu"));
        }
    }

    private static Pokemon seed(int id, String name, String category, int weight, int height,
                                List<String> abilities, String localized) {
        return new Pokemon(id, name, SPRITE.formatted(id), category, weight, height, abilities,
                localized, "Kanto", List.of("starter-demo"), null, null, null);
    }
}
