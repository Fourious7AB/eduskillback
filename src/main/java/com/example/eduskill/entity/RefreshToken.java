package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refresh_token",indexes = {
        @Index(name = "refresh_tokens_jti_idx",columnList = "jti",unique = true),
        @Index(name = "refresh_tokens_user_id_idx",columnList = "user_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "jti",unique = true,nullable = false,updatable = false)
    private String jti;

    @ManyToOne(optional = false,fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",nullable = false,updatable = false)
    private User user;

    @Column(updatable = false,nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiredAt;

    @Column(nullable = false)
    private boolean revoked;

    // private String refreshToken;

    private String replacedByToken;




}
