package com.example.pokedex.application.port;

import com.example.pokedex.domain.model.User;

public interface TokenIssuer {
    AuthToken issue(User user);

    record AuthToken(String value, long expiresInSeconds) { }
}
