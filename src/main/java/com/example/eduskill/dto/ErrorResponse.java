package com.example.eduskill.dto;

public record ErrorResponse(
        String message,
        int status,
        String error
) {
}
