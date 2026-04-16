package com.example.eduskill.repository;

import com.example.eduskill.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Query("""
    SELECT COALESCE(SUM(p.amount), 0)
    FROM Payment p
    WHERE p.subscription.enrollment.employeeCode = :employeeCode
      AND p.paymentDate >= :startOfMonth
      AND p.paymentDate < :startOfNextMonth
""")
    Long monthlyRevenueByEmployee(@Param("employeeCode") String employeeCode,
                                  @Param("startOfMonth") LocalDateTime startOfMonth,
                                  @Param("startOfNextMonth") LocalDateTime startOfNextMonth);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p")
    Long totalRevenue();

    @Query("""
SELECT COALESCE(SUM(p.amount),0) FROM Payment p
WHERE p.paymentDate >= :start AND p.paymentDate < :end
""")
    Long monthlyRevenue(LocalDateTime start, LocalDateTime end);

    @Query("""
SELECT COALESCE(SUM(p.amount),0) FROM Payment p
WHERE p.paymentDate >= :start
""")
    Long revenueFromDate(LocalDateTime start);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.subscription.enrollment.employeeCode = :employeeCode")
    Long totalRevenueByEmployee(@Param("employeeCode") String employeeCode);

    Optional<Payment> findByRazorpayPaymentId(String razorpayPaymentId);

    boolean existsByRazorpayPaymentId(String razorpayPaymentId);
}