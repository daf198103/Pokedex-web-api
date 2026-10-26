package com.example.pokedex.domain.model;

public record User(Long id, String username, String email, String passwordHash, String role) { }
