package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "subscription_payment_history")
public class SubscriptionPaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many payment records for one subscription
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    private String razorpayPaymentId;

    private Integer amount;

    private LocalDateTime paymentDate;

    // Previous due date
    private LocalDateTime previousPaymentDate;

    // Newly generated next due date
    private LocalDateTime newNextPaymentDate;

    private String invoiceNumber;

    private String paymentStatus;
}