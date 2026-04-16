package com.example.eduskill.events;

import com.example.eduskill.dto.request.PaymentCallbackRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {

    private PaymentCallbackRequest request;

}