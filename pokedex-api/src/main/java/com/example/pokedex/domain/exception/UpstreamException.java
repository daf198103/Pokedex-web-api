package com.example.pokedex.domain.exception;

public class UpstreamException extends RuntimeException {
    public UpstreamException(String message) {
        super(message);
    }
}
