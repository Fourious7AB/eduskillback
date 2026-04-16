package com.example.eduskill.dto.request;

import lombok.Data;
import org.json.JSONObject; // or you can use Map<String, String>

@Data
public class PaymentCallbackRequest {

    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    private String studentName;
    private String email;
    private String phone;
    private String courseName;
    private int amount;

    // ✅ Add this field for Razorpay payment notes
    private JSONObject notes; // you can also use Map<String, String> if you prefer
}