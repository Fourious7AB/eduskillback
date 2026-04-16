package com.example.eduskill.repository;

import com.example.eduskill.entity.RefreshToken;
import com.example.eduskill.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByJti(String jti);
    void deleteByUser(User user);
}
