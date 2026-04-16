package com.example.eduskill.service;

import com.example.eduskill.dto.request.PaymentCallbackRequest;
import com.example.eduskill.entity.Subscription;

public interface PaymentService {

    boolean verifyPayment(PaymentCallbackRequest request);
    String generateRenewalPaymentLink(Subscription subscription);
    void processWebhook(String payload, String signature);

    //boolean verifyWebhookSignature(String payload, String signature);

}