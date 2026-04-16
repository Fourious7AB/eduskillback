package com.example.eduskill.dto.dashboard;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesDashboardResponse {

    private Long totalSales;
    private Long monthlySales;

    private Long totalRevenue;
    private Long monthlyRevenue;
}
