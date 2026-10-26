package com.example.pokedex.application.usecase;

import com.example.pokedex.application.port.PasswordHasher;
import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.domain.exception.ConflictException;
import com.example.pokedex.domain.exception.ValidationException;
import com.example.pokedex.domain.model.User;

import java.util.regex.Pattern;

public class RegisterUserUseCase {
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_.-]{3,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final UserRepository users;
    private final PasswordHasher hasher;

    public RegisterUserUseCase(UserRepository users, PasswordHasher hasher) {
        this.users = users;
        this.hasher = hasher;
    }

    public User execute(String username, String email, String rawPassword) {
        String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();

        if (!USERNAME.matcher(normalizedUsername).matches()) {
            throw new ValidationException("username must be 3-50 chars: letters, digits, '.', '_' or '-'");
        }
        if (!EMAIL.matcher(normalizedEmail).matches()) {
            throw new ValidationException("email is not valid");
        }
        if (rawPassword == null || rawPassword.length() < 8 || rawPassword.length() > 72) {
            throw new ValidationException("password must be between 8 and 72 characters");
        }
        if (users.existsByUsername(normalizedUsername)) {
            throw new ConflictException("username already taken");
        }
        if (users.existsByEmail(normalizedEmail)) {
            throw new ConflictException("email already registered");
        }
        return users.save(new User(null, normalizedUsername, normalizedEmail, hasher.hash(rawPassword), "USER"));
    }
}
