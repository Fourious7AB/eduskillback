package com.example.eduskill.helper;

import com.example.eduskill.entity.Enrollment;
import com.example.eduskill.entity.Subscription;
import com.example.eduskill.entity.SubscriptionStatus;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionHelperService {

    public Subscription getActiveSubscription(Enrollment enrollment) {
        return enrollment.getSubscriptions()
                .stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
}