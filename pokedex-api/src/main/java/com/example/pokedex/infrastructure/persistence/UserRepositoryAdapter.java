package com.example.pokedex.infrastructure.persistence;

import com.example.pokedex.application.port.UserRepository;
import com.example.pokedex.domain.model.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        UserEntity e = new UserEntity();
        e.setUsername(user.username());
        e.setEmail(user.email());
        e.setPasswordHash(user.passwordHash());
        e.setRole(user.role());
        return toDomain(jpa.save(e));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpa.findByUsername(username).map(UserRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpa.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private static User toDomain(UserEntity e) {
        return new User(e.getId(), e.getUsername(), e.getEmail(), e.getPasswordHash(), e.getRole());
    }
}
