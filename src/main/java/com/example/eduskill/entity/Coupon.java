package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String code;

    private Integer discountPercent;

    private Integer discountAmount;

    private Integer maxUses;

    private Integer usedCount;

    private LocalDate expiryDate;

    @ManyToOne
    private Course course;
}