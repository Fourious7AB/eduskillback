package com.example.eduskill.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployeeSalesByNameDTO {

    private String name;
    private String employeeCode;

    private Long totalSales;
    private Long weeklySales;
    private Long monthlySales;

    private Long totalRevenue;
    private Long weeklyRevenue;
    private Long monthlyRevenue;
}
