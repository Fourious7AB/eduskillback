package com.example.eduskill.controller;

import com.example.eduskill.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/webhook")
@Slf4j
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;
    @PostMapping("/razorpay")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Razorpay-Signature") String signature
    ) {

        log.info("Webhook received");

        paymentService.processWebhook(payload, signature);

        return ResponseEntity.ok("Webhook received");
    }
}