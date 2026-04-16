package com.example.eduskill.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {

    private String orderId;

    private Integer amount;

    private String key;

}