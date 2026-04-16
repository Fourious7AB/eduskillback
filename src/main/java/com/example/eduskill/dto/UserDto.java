package com.example.eduskill.dto;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    private UUID id;

    private String name;
    private String employeeCode;

    private String email;
    private String password;
    private String image;
    private Boolean enable=true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> roles=new HashSet<>();
}
