package com.example.eduskill.dto.dashboard;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SalesLeaderboardDTO {

    private String employeeCode;
    private Long totalSales;
    private Long monthlySales;
}
