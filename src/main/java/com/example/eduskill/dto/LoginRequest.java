package com.example.eduskill.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        String employeeCode,
        String password
) {}
