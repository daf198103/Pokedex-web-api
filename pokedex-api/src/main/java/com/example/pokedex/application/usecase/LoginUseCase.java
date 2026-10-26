package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.PasswordHasher;
import com.example.pokedex.application.port.TokenIssuer;
import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.domain.exception.InvalidCredentialsException;
import com.example.pokedex.domain.model.User;

public class LoginUseCase {
    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokens;

    public LoginUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokens) {
        this.users = users;
        this.hasher = hasher;
        this.tokens = tokens;
    }

    public record LoginResult(TokenIssuer.AuthToken token, User user) { }

    public LoginResult execute(String username, String rawPassword) {
        String normalized = username == null ? "" : username.trim().toLowerCase();
        User user = users.findByUsername(normalized)
                .filter(u -> rawPassword != null && hasher.matches(rawPassword, u.passwordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("invalid username or password"));
        return new LoginResult(tokens.issue(user), user);
    }
}
