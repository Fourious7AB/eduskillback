package com.example.eduskill.security;

import com.example.eduskill.entity.User;
import com.example.eduskill.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeCode) {

        return userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found: " + employeeCode)
                );
    }
}