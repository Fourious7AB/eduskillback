package com.example.eduskill.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminDashboardDTO {

    private Long totalStudents;
    private Long activeStudents;
    private Long disabledStudents;

    private Long totalCourses;

    private Double totalRevenue;
    private Double monthlyRevenue;

    private Long successfulPayments;
    private Long failedPayments;

}