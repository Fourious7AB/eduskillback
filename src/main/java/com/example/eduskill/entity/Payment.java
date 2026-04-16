package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;



@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String razorpayPaymentId;

    private String razorpayOrderId;

    @Column(name = "payment_status")
    private String paymentStatus; // SUCCESS / FAILED

    private Integer amount;

    private LocalDateTime paymentDate;

    private String invoiceNumber;

    // This section is not working i will fix it later
    private String studentName;
    private String email;
    private String phone;
    private String courseName;
    //i have not time to fix it

    @ManyToOne
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
}