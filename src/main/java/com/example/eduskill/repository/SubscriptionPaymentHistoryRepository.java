package com.example.eduskill.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.example.eduskill.entity.Subscription;
import com.example.eduskill.entity.SubscriptionPaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPaymentHistoryRepository
        extends JpaRepository<SubscriptionPaymentHistory, Long> {

    Optional<SubscriptionPaymentHistory> findTopBySubscriptionOrderByPaymentDateDesc(Subscription sub);
}