package com.example.eduskill.service.impl;


import com.example.eduskill.dto.UserDto;
import com.example.eduskill.entity.Role;
import com.example.eduskill.entity.User;
import com.example.eduskill.exception.ResourseNotFoundException;
import com.example.eduskill.helper.UserHelper;
import com.example.eduskill.repository.RefreshTokenRepository;
import com.example.eduskill.repository.RoleRepository;
import com.example.eduskill.repository.UserRepository;
import com.example.eduskill.service.UserService;
import jakarta.persistence.GeneratedValue;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // ----------------------------
        // CREATE USER MANUALLY (SAFE)
        // ----------------------------
        User user = new User();

        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setImage(userDto.getImage());
        user.setEmployeeCode(generateEmployeeCode(user.getName()));
        user.setEnable(true);
        user.setActive(true);

        // ----------------------------
        // ROLE HANDLING (FIXED)
        // ----------------------------
        Set<Role> roles = new HashSet<>();

        // If frontend does NOT send roles → default STUDENT
        if (userDto.getRoles() == null || userDto.getRoles().isEmpty()) {

            Role studentRole = roleRepository.findByName("STUDENT")
                    .orElseThrow(() -> new RuntimeException("STUDENT role not found"));

            roles.add(studentRole);

        } else {

            // If frontend sends role names → fetch from DB safely
            roles = userDto.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                    .collect(Collectors.toSet());
        }

        user.setRoles(roles);

        // ----------------------------
        // SAVE USER
        // ----------------------------
        User savedUser = userRepository.save(user);

        // ----------------------------
        // RESPONSE DTO
        // ----------------------------
        UserDto dto = modelMapper.map(savedUser, UserDto.class);

        dto.setRoles(
                savedUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );

        return dto;
    }

    @Override
    public Iterable<UserDto> getAllUser() {

        return userRepository.findAll()
                .stream()
                .map(user -> {
                    UserDto dto = modelMapper.map(user, UserDto.class);

                    dto.setRoles(
                            user.getRoles()
                                    .stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toSet())
                    );

                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {

        UUID uId = UserHelper.parseUUID(userId);

        User existingUser = userRepository.findById(uId)
                .orElseThrow(() -> new ResourseNotFoundException("User not found"));

        // ✅ NAME
        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        // ✅ EMAIL (ADD THIS 🔥)
        if (userDto.getEmail() != null) {
            existingUser.setEmail(userDto.getEmail());
        }

        // ✅ IMAGE
        if (userDto.getImage() != null) {
            existingUser.setImage(userDto.getImage());
        }

        // ✅ ENABLE
        if (userDto.getEnable() != null) {
            existingUser.setEnable(userDto.getEnable());
        }

        // ✅ PASSWORD (SAFE)
        if (userDto.getPassword() != null && !userDto.getPassword().isBlank()) {
            if (!userDto.getPassword().startsWith("$2a$")) {
                existingUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
        }

        // ❌ DO NOT CHANGE employeeCode

        User updatedUser = userRepository.save(existingUser);

        UserDto dto = modelMapper.map(updatedUser, UserDto.class);

        dto.setRoles(
                updatedUser.getRoles()
                        .stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet())
        );

        return dto;
    }

    @Override
    public UserDto getUserByEmail(String email) {
        return null;
    }

    @Override
    @Transactional
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);

        User user = userRepository.findById(uId)
                .orElseThrow(() -> new ResourseNotFoundException("User not found"));

        // 🔥 STEP 1: REMOVE ROLES
        user.getRoles().clear();
        userRepository.save(user);

        // 🔥 STEP 2: DELETE REFRESH TOKENS (IMPORTANT)
        refreshTokenRepository.deleteByUser(user);

        // 🔥 STEP 3: DELETE USER
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        UUID uId= UserHelper.parseUUID(userId);
        User user=userRepository.findById(uId)
                .orElseThrow(()->new ResourseNotFoundException("User not found by this providing Id"));

        return modelMapper.map(user,UserDto.class);
    }
    private String generateEmployeeCode(String name) {
        String prefix = name == null ? "EMP" : name.substring(0, Math.min(3, name.length())).toUpperCase();
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return prefix + "-" + random;
    }



    @Override
    public Page<UserDto> getEnabledUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByEnableTrue(pageable)
                .map(user -> {
                    UserDto dto = modelMapper.map(user, UserDto.class);

                    dto.setRoles(
                            user.getRoles()
                                    .stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toSet())
                    );

                    return dto;
                });
    }

    @Override
    public Page<UserDto> getDisabledUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findByEnableFalse(pageable)
                .map(user -> {
                    UserDto dto = modelMapper.map(user, UserDto.class);

                    dto.setRoles(
                            user.getRoles()
                                    .stream()
                                    .map(Role::getName)
                                    .collect(Collectors.toSet())
                    );

                    return dto;
                });
    }


}