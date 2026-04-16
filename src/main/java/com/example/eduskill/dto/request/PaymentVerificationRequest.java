package com.example.eduskill.dto.request;

import lombok.Data;

@Data
public class PaymentVerificationRequest {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    // 🔥 extra fields from frontend
    private String studentName;
    private String email;
    private String phone;
    private String courseName;
    private Integer amount;

}