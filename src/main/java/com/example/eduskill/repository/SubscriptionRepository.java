package com.example.eduskill.repository;

import com.example.eduskill.entity.Subscription;
import com.example.eduskill.entity.Enrollment;
import com.example.eduskill.projection.DirectorDashboardProjection;
import com.example.eduskill.projection.SubscriptionStatsProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription,Long> {

    List<Subscription> findByActiveTrue();
    List<Subscription> findByNextPaymentDateBeforeAndActiveTrue(LocalDateTime date);
    long countByActiveTrue();
    long countByActiveFalse();
    Optional<Subscription> findByEnrollment(Enrollment enrollment);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Subscription> findById(Long id);

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.active = true
        AND s.nextPaymentDate <= :time
    """)
    List<Subscription> findExpiringSubscriptions(LocalDateTime time);



    @Query("""
SELECT
    COUNT(s) AS totalSubscriptions,
    COALESCE(SUM(CASE WHEN s.active = true THEN 1 ELSE 0 END),0) AS activeSubscriptions,
    COALESCE(SUM(CASE WHEN s.nextPaymentDate < CURRENT_TIMESTAMP THEN 1 ELSE 0 END),0) AS expiredSubscriptions
FROM Subscription s
""")
    SubscriptionStatsProjection getSubscriptionStats();
}