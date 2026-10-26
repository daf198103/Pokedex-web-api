package com.example.pokedex.application;

import com.example.pokedex.application.port.PasswordHasher;
import com.example.pokedex.application.port.TokenIssuer;
import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.application.usecase.LoginUseCase;
import com.example.pokedex.application.usecase.RegisterUserUseCase;
import com.example.pokedex.domain.exception.ConflictException;
import com.example.pokedex.domain.exception.InvalidCredentialsException;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCasesTest {
    @Mock UserRepository users;
    @Mock PasswordHasher hasher;
    @Mock TokenIssuer tokens;

    @Test
    void registerHashesPasswordAndNormalizesInput() {
        when(users.existsByUsername("ash")).thenReturn(false);
        when(users.existsByEmail("ash@example.com")).thenReturn(false);
        when(hasher.hash("secret123")).thenReturn("HASH");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User user = new RegisterUserUseCase(users, hasher).execute(" Ash ", "ASH@example.com", "secret123");

        assertEquals("ash", user.username());
        assertEquals("HASH", user.passwordHash());
        assertEquals("USER", user.role());
    }

    @Test
    void registerRejectsDuplicatesAndWeakInput() {
        var useCase = new RegisterUserUseCase(users, hasher);
        assertThrows(ValidationException.class, () -> useCase.execute("ab", "a@b.com", "secret123"));
        assertThrows(ValidationException.class, () -> useCase.execute("ash", "not-an-email", "secret123"));
        assertThrows(ValidationException.class, () -> useCase.execute("ash", "a@b.com", "short"));

        when(users.existsByUsername("ash")).thenReturn(true);
        assertThrows(ConflictException.class, () -> useCase.execute("ash", "a@b.com", "secret123"));
    }

    @Test
    void loginIssuesTokenOnValidCredentials() {
        User user = new User(1L, "ash", "a@b.com", "HASH", "USER");
        when(users.findByUsername("ash")).thenReturn(Optional.of(user));
        when(hasher.matches("secret123", "HASH")).thenReturn(true);
        when(tokens.issue(user)).thenReturn(new TokenIssuer.AuthToken("jwt", 3600));

        var result = new LoginUseCase(users, hasher, tokens).execute("Ash", "secret123");

        assertEquals("jwt", result.token().value());
    }

    @Test
    void loginFailsOnUnknownUserOrWrongPassword() {
        var useCase = new LoginUseCase(users, hasher, tokens);
        when(users.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute("ghost", "x"));

        User user = new User(1L, "ash", "a@b.com", "HASH", "USER");
        when(users.findByUsername("ash")).thenReturn(Optional.of(user));
        when(hasher.matches("wrong", "HASH")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> useCase.execute("ash", "wrong"));
    }
}
