package com.example.eduskill.dto;

import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        String tokenType,
        UserDto user

) {
    public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn, UserDto user){
        return new TokenResponse(accessToken,refreshToken,expiresIn,"Bearer",user);
    }
}
