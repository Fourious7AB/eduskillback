package com.example.eduskill.service;

import com.example.eduskill.entity.Subscription;

public interface SubscriptionService {

    Subscription createSubscription(Subscription subscription);

    void extendSubscription(Subscription subscription);

    void disableSubscription(Subscription subscription);

    void markAsPaymentPending(Subscription sub);

    void markAsPaymentFailed(Subscription sub);
}