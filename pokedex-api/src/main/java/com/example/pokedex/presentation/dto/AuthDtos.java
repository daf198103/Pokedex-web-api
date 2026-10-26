package com.example.pokedex.presentation.dto;

import com.example.pokedex.domain.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() { }

    public record RegisterRequest(@NotBlank @Size(min = 3, max = 50) String username,
                                  @NotBlank @Email @Size(max = 150) String email,
                                  @NotBlank @Size(min = 8, max = 72) String password) { }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) { }

    public record UserResponse(Long id, String username, String email, String role) {
        public static UserResponse from(User u) {
            return new UserResponse(u.id(), u.username(), u.email(), u.role());
        }
    }

    public record AuthResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) { }
}
