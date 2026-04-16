package com.example.eduskill.seeder;

import com.example.eduskill.entity.Role;
import com.example.eduskill.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {

        List<String> roles = List.of(
                "ADMIN","DIRECTOR","STUDENT","CEO","CTO","CFO","CLO","CMO",
                "BDM","BDE","TC","BDE_TL","COO","CRM",
                "CTO_E","CFO_E","CLO_E","CMO_E",
                "BDM_E","BDE_E","TC_E","BDE_TL_E","COO_E"
        );

        roles.forEach(roleName -> {

            roleRepository.findByName(roleName)
                    .orElseGet(() -> {

                        Role role = new Role();
                        role.setName(roleName);
                        return roleRepository.save(role);
                    });

        });

        System.out.println("Roles inserted successfully");
    }
}