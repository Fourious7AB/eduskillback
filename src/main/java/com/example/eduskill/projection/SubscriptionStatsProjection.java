package com.example.eduskill.projection;

public interface SubscriptionStatsProjection {
    Long getTotalSubscriptions();
    Long getActiveSubscriptions();
    Long getExpiredSubscriptions();
}
