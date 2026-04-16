package com.example.eduskill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesmanStatsDTO {

    private String referralCode;

    private Long totalAmount;

    private Long sales7Days;
    private Long sales30Days;
    private Long sales1Year;
}
