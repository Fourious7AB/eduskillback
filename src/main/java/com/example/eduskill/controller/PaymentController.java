package com.example.eduskill.controller;

import com.example.eduskill.dto.request.PaymentCallbackRequest;
import com.example.eduskill.dto.request.PaymentVerificationRequest;
import com.example.eduskill.dto.response.PaymentResponse;
import com.example.eduskill.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @RequestBody PaymentVerificationRequest request
    ) {

        // 🔥 Convert to your existing DTO
        PaymentCallbackRequest callback = new PaymentCallbackRequest();

        callback.setRazorpayOrderId(request.getRazorpayOrderId());
        callback.setRazorpayPaymentId(request.getRazorpayPaymentId());
        callback.setRazorpaySignature(request.getRazorpaySignature());

        // 🔥 Pass extra data
        callback.setStudentName(request.getStudentName());
        callback.setEmail(request.getEmail());
        callback.setPhone(request.getPhone());
        callback.setCourseName(request.getCourseName());

        // Optional notes
        JSONObject notes = new JSONObject();
        callback.setNotes(notes);

        boolean success = paymentService.verifyPayment(callback);

        if (success) {
            return ResponseEntity.ok(
                    PaymentResponse.builder()
                            .status("SUCCESS")
                            .message("Payment verified successfully")
                            .build()
            );
        } else {
            return ResponseEntity.badRequest().body(
                    PaymentResponse.builder()
                            .status("FAILED")
                            .message("Payment verification failed")
                            .build()
            );
        }
    }

}