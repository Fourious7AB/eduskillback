package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "code_sequence")
public class CodeSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String prefix;

    private Long seq;

}