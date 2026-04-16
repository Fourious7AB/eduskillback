package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
@Table(name = "payment_orders")
public class PaymentOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String razorpayOrderId;

    private Integer amount;

    private String orderStatus;


    private String studentName;
    private String phone;
    private String whatsapp;
    private String email;

    private String courseName;
    private String studentClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;



}