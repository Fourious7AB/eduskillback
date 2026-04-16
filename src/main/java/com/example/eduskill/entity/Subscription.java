package com.example.eduskill.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(
        name="subscription",
        indexes = {
                @Index(name="idx_next_payment", columnList="nextPaymentDate"),
                @Index(name="idx_active", columnList="active")
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;


    private LocalDateTime startDate;

    private LocalDateTime nextPaymentDate;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    // prevents repeated reminder emails
    private boolean reminderSent = false;

    private String renewalPaymentLink;

    private Integer retryCount = 0;

    private LocalDateTime lastRetryDate;



    /**
     * Determine if a reminder should be sent
     */
    public boolean shouldSendReminder(LocalDateTime now, boolean testMode) {

        if (!active || reminderSent || nextPaymentDate == null || now == null)
            return false;

        long minutesBefore = testMode ? 3 : 1440;

        LocalDateTime reminderTime = nextPaymentDate.minusMinutes(minutesBefore);

        return now.isAfter(reminderTime) && now.isBefore(nextPaymentDate);
    }

    /**
     * Check if subscription expired
     */
    public boolean isExpired(LocalDateTime now) {

        return nextPaymentDate != null &&
                now != null &&
                now.isAfter(nextPaymentDate);
    }

    /**
     * Reset reminder flag
     */
    public void resetReminder() {
        this.reminderSent = false;
    }

    /**
     * Extend next payment date
     */
    public void extendNextPayment(LocalDateTime now, boolean testMode) {

        LocalDateTime base =
                (nextPaymentDate == null || nextPaymentDate.isBefore(now))
                        ? now
                        : nextPaymentDate;

        this.nextPaymentDate = testMode
                ? base.plusMinutes(10)
                : base.plusDays(30);

        resetReminder();

        this.retryCount = 0;

        this.renewalPaymentLink = null;

        this.active = true;
    }
}