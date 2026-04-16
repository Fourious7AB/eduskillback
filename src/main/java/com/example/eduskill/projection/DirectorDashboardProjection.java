package com.example.eduskill.projection;

public interface DirectorDashboardProjection {

    Long getTotalRevenue();
    Long getMonthlyRevenue();
    Long getTotalSales();
    Long getMonthlySales();
    Long getTotalStudents();
    Long getNewStudents();
    Long getActiveStudents();
    Long getTotalSubscriptions();
    Long getActiveSubscriptions();
    Long getExpiredSubscriptions();

}