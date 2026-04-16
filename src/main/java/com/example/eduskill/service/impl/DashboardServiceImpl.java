package com.example.eduskill.service.impl;

import com.example.eduskill.dto.SalesHistoryDTO;
import com.example.eduskill.dto.SalesmanListDTO;
import com.example.eduskill.dto.SalesmanStatsDTO;
import com.example.eduskill.dto.dashboard.*;
import com.example.eduskill.entity.Enrollment;
import com.example.eduskill.projection.*;
import com.example.eduskill.repository.*;
import com.example.eduskill.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public DirectorDashboardResponse directorDashboard() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        LocalDateTime last7Days = now.minusDays(7);
        LocalDateTime last30Days = now.minusDays(30);
        LocalDateTime last1Year = now.minusYears(1);

        // ✅ FIXED PROJECTIONS
        StudentStatsProjection studentStats = studentRepository.getStudentStats(startOfMonth);
        SubscriptionStatsProjection subscriptionStats = subscriptionRepository.getSubscriptionStats();

        // ✅ REVENUE
        Long totalRevenue = safe(paymentRepository.totalRevenue());
        Long monthlyRevenue = safe(paymentRepository.monthlyRevenue(startOfMonth, startOfNextMonth));
        Long revenue7Days = safe(paymentRepository.revenueFromDate(last7Days));
        Long revenue30Days = safe(paymentRepository.revenueFromDate(last30Days));
        Long revenue1Year = safe(paymentRepository.revenueFromDate(last1Year));

        // ✅ SALES
        Long totalSales = safe(enrollmentRepository.totalSales());
        Long monthlySales = safe(enrollmentRepository.monthlySales(startOfMonth, startOfNextMonth));
        Long sales7Days = safe(enrollmentRepository.salesFromDate(last7Days));
        Long sales30Days = safe(enrollmentRepository.salesFromDate(last30Days));
        Long sales1Year = safe(enrollmentRepository.salesFromDate(last1Year));

        // ✅ GROWTH
        Double revenueGrowth = totalRevenue == 0 ? 0 :
                ((double) monthlyRevenue / totalRevenue) * 100;

        Double salesGrowth = totalSales == 0 ? 0 :
                ((double) monthlySales / totalSales) * 100;

        // ✅ RENEWAL RATE
        Double renewalRate = subscriptionStats.getTotalSubscriptions() == 0
                ? 0
                : (double) subscriptionStats.getActiveSubscriptions()
                / subscriptionStats.getTotalSubscriptions() * 100;

        // ✅ TOP SALESMAN
        List<SalesByReferralProjection> leaderboard =
                enrollmentRepository.topSalesByReferral(PageRequest.of(0, 10));

        List<SalesLeaderboardDTO> topSalesman = leaderboard.stream()
                .map(p -> SalesLeaderboardDTO.builder()
                        .employeeCode(p.getReferralCode())
                        .totalSales(safe(p.getTotalSales()))
                        .monthlySales(safe(
                                enrollmentRepository.monthlySalesByReferral(
                                        p.getReferralCode(),
                                        startOfMonth,
                                        startOfNextMonth
                                )
                        ))
                        .build())
                .toList();

        // ✅ TOP COURSES
        List<CourseSalesDTO> topCourses = enrollmentRepository
                .topCourses(PageRequest.of(0, 3))
                .stream()
                .map(obj -> CourseSalesDTO.builder()
                        .courseName((String) obj[0])
                        .totalSales((Long) obj[1])
                        .build())
                .toList();

        return DirectorDashboardResponse.builder()
                .totalRevenue(totalRevenue)
                .monthlyRevenue(monthlyRevenue)
                .revenue7Days(revenue7Days)
                .revenue30Days(revenue30Days)
                .revenue1Year(revenue1Year)
                .revenueGrowth(revenueGrowth)

                .totalSales(totalSales)
                .monthlySales(monthlySales)
                .sales7Days(sales7Days)
                .sales30Days(sales30Days)
                .sales1Year(sales1Year)
                .salesGrowth(salesGrowth)

                .totalStudents(safe(studentStats.getTotalStudents()))
                .newStudentsThisMonth(safe(studentStats.getNewStudents()))
                .activeStudents(safe(studentStats.getActiveStudents()))

                .totalSubscriptions(safe(subscriptionStats.getTotalSubscriptions()))
                .activeSubscriptions(safe(subscriptionStats.getActiveSubscriptions()))
                .expiredSubscriptions(safe(subscriptionStats.getExpiredSubscriptions()))
                .renewalRate(renewalRate)

                .topSalesman(topSalesman)
                .topCourses(topCourses)

                .build();
    }

    // ✅ NULL FIX
    private Long safe(Long value) {
        return value == null ? 0L : value;
    }

    // ✅ EXTRA FILTER METHOD
    @Override
    public DirectorDashboardResponse directorDashboardByDays(int days) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(days);

        return DirectorDashboardResponse.builder()

                // ONLY FILTERED DATA
                .totalRevenue(safe(paymentRepository.revenueFromDate(start)))
                .totalSales(safe(enrollmentRepository.salesFromDate(start)))

                // IMPORTANT: ALSO SET THESE (THIS WAS MISSING)
                .revenue7Days(days == 7 ? safe(paymentRepository.revenueFromDate(start)) : 0L)
                .revenue30Days(days == 30 ? safe(paymentRepository.revenueFromDate(start)) : 0L)
                .revenue1Year(days == 365 ? safe(paymentRepository.revenueFromDate(start)) : 0L)

                .sales7Days(days == 7 ? safe(enrollmentRepository.salesFromDate(start)) : 0L)
                .sales30Days(days == 30 ? safe(enrollmentRepository.salesFromDate(start)) : 0L)
                .sales1Year(days == 365 ? safe(enrollmentRepository.salesFromDate(start)) : 0L)

                .build();
    }

    @Override
    public SalesDashboardResponse salesmanDashboard(String referralCode) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startOfMonth = now.withDayOfMonth(1)
                .withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime startOfNextMonth = startOfMonth.plusMonths(1);

        Long totalSales = safe(
                enrollmentRepository.monthlySalesByReferral(
                        referralCode,
                        LocalDateTime.MIN,
                        LocalDateTime.now()
                )
        );

        Long monthlySales = safe(
                enrollmentRepository.monthlySalesByReferral(
                        referralCode,
                        startOfMonth,
                        startOfNextMonth
                )
        );

        return SalesDashboardResponse.builder()
                .totalSales(totalSales)
                .monthlySales(monthlySales)
                .totalRevenue(0L) // (optional: can implement later)
                .monthlyRevenue(0L)
                .build();
    }

    @Override
    public List<SalesmanListDTO> getAllSalesmans() {

        List<SalesByReferralProjection> data =
                enrollmentRepository.topSalesByReferral(PageRequest.of(0, 50));

        return data.stream()
                .map(p -> SalesmanListDTO.builder()
                        .referralCode(p.getReferralCode())
                        .totalSales(p.getTotalSales())
                        .build())
                .toList();
    }

    @Override
    public SalesmanStatsDTO getSalesmanStats(String referralCode) {

        LocalDateTime now = LocalDateTime.now();

        Long totalAmount = safe(
                enrollmentRepository.totalAmountByReferral(referralCode)
        );

        Long sales7Days = safe(
                enrollmentRepository.salesFromDateByReferral(
                        referralCode, now.minusDays(7))
        );

        Long sales30Days = safe(
                enrollmentRepository.salesFromDateByReferral(
                        referralCode, now.minusDays(30))
        );

        Long sales1Year = safe(
                enrollmentRepository.salesFromDateByReferral(
                        referralCode, now.minusYears(1))
        );

        return SalesmanStatsDTO.builder()
                .referralCode(referralCode)
                .totalAmount(totalAmount)
                .sales7Days(sales7Days)
                .sales30Days(sales30Days)
                .sales1Year(sales1Year)
                .build();
    }

    @Override
    public Page<SalesHistoryDTO> getSalesHistory(String referralCode, int page) {

        Page<Enrollment> enrollments =
                enrollmentRepository.findSalesByReferral(
                        referralCode,
                        PageRequest.of(page, 7)
                );

        return enrollments.map(e -> SalesHistoryDTO.builder()
                .studentName(e.getStudent().getName())
                .studentEmail(e.getStudent().getEmail())
                .courseName(e.getCourse().getCourseName())
                .amount(e.getCourse().getJoiningFee())
                .saleDate(e.getEnrolledDate())
                .build()
        );
    }



}