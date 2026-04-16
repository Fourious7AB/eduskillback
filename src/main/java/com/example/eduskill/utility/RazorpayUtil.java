package com.example.eduskill.utility;

import com.razorpay.Utils;

public class RazorpayUtil {

    public static void verifySignature(
            String orderId,
            String paymentId,
            String signature,
            String secret
    ) {

        try {

            String payload = orderId + "|" + paymentId;

            Utils.verifySignature(payload, signature, secret);

        } catch (Exception e) {

            throw new RuntimeException("Invalid Razorpay Signature");
        }
    }
}