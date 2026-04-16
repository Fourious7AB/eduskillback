package com.example.eduskill.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class PaymentFailureQueue {

    @Id
    @GeneratedValue
    private Long id;

    private String payload;

    private String signature;

    private int retryCount;

    private LocalDateTime createdAt;

}
