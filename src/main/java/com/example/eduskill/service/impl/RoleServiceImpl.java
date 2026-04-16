package com.example.eduskill.service.impl;

import com.example.eduskill.repository.RoleRepository;
import com.example.eduskill.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<String> getAllRoles() {

        return roleRepository.findAll()
                .stream()
                .map(role -> role.getName())
                .toList();
    }
}
