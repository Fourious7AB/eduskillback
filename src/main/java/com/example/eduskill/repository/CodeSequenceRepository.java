package com.example.eduskill.repository;

import com.example.eduskill.entity.CodeSequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodeSequenceRepository extends JpaRepository<CodeSequence, Long> {

    Optional<CodeSequence> findByPrefix(String prefix);

}