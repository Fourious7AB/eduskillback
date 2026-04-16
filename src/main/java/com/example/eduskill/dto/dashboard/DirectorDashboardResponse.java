package com.example.eduskill.dto.dashboard;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectorDashboardResponse {

    // Revenue
    private Long totalRevenue;
    private Long monthlyRevenue;
    private Long subscriptionRevenue;
    private Long monthlySubscriptionRevenue;
    private Double revenueGrowth;

    // Sales
    private Long totalSales;
    private Long monthlySales;
    private Double salesGrowth;

    // Users
    private Long totalStudents;
    private Long newStudentsThisMonth;
    private Long activeStudents;

    // Subscription
    private Long totalSubscriptions;
    private Long activeSubscriptions;
    private Long expiredSubscriptions;
    private Double renewalRate;

    // Leaderboard
    private List<SalesLeaderboardDTO> topSalesman;
    private Long revenue7Days;
    private Long revenue30Days;
    private Long revenue1Year;

    private Long sales7Days;
    private Long sales30Days;
    private Long sales1Year;

    private List<CourseSalesDTO> topCourses;
}
