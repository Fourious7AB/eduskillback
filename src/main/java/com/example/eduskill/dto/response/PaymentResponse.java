package com.example.eduskill.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponse {
    private String message;
    private String status;
}
