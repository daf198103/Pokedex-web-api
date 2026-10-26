package com.example.pokedex.presentation.controller;

import com.example.pokedex.application.usecase.LoginUseCase;
import com.example.pokedex.application.usecase.RegisterUserUseCase;
import com.example.pokedex.presentation.dto.AuthDtos.AuthResponse;
import com.example.pokedex.presentation.dto.AuthDtos.LoginRequest;
import com.example.pokedex.presentation.dto.AuthDtos.RegisterRequest;
import com.example.pokedex.presentation.dto.AuthDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final RegisterUserUseCase register;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase register, LoginUseCase login) {
        this.register = register;
        this.login = login;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(register.execute(request.username(), request.email(), request.password()));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = login.execute(request.username(), request.password());
        return new AuthResponse(result.token().value(), "Bearer", result.token().expiresInSeconds(),
                UserResponse.from(result.user()));
    }
}
