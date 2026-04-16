package com.example.eduskill.service.impl;


import com.example.eduskill.dto.UserDto;
import com.example.eduskill.service.AuthService;
import com.example.eduskill.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceInple implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserDto registerUser(UserDto userDto) {

        return userService.createUser(userDto);
    }
}
