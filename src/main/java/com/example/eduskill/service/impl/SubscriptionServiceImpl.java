package com.example.eduskill.service.impl;

import com.example.eduskill.entity.Enrollment;
import com.example.eduskill.entity.Student;
import com.example.eduskill.entity.Subscription;
import com.example.eduskill.entity.SubscriptionStatus;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.StudentRepository;
import com.example.eduskill.repository.SubscriptionPaymentHistoryRepository;
import com.example.eduskill.repository.SubscriptionRepository;
import com.example.eduskill.service.EmailService;
import com.example.eduskill.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final StudentRepository studentRepository;
    private final EmailService emailService;
    private final SubscriptionPaymentHistoryRepository historyRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${subscription.test-mode:true}")
    private boolean testMode;

    /**
     * Create new subscription after payment success
     */
    @Override
    public Subscription createSubscription(Subscription subscription) {

        subscription.setStartDate(LocalDateTime.now());

        if (testMode) {
            subscription.setNextPaymentDate(LocalDateTime.now().plusMinutes(10));
        } else {
            subscription.setNextPaymentDate(LocalDateTime.now().plusDays(30));
        }

        subscription.setReminderSent(false);
        subscription.setRetryCount(0);
        subscription.setActive(true);

        // ✅ Set subscription status for state machine
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription saved = subscriptionRepository.save(subscription);

        log.info("Subscription created for {} with status {}",
                saved.getEnrollment().getStudent().getEmail(),
                saved.getStatus());

        return saved;
    }

    /**
     * Extend subscription after renewal payment
     */
    @Override
    public void extendSubscription(Subscription sub) {

        LocalDateTime previousDate = Optional.ofNullable(sub.getNextPaymentDate())
                .filter(d -> d.isAfter(LocalDateTime.now()))
                .orElse(LocalDateTime.now());

        LocalDateTime newNext = testMode
                ? previousDate.plusMinutes(10)
                : previousDate.plusDays(30);

        sub.setNextPaymentDate(newNext);

        sub.resetReminder();
        sub.setRetryCount(0);
        sub.setRenewalPaymentLink(null);
        sub.setActive(true);

        // ✅ Set status to ACTIVE after successful renewal
        sub.setStatus(SubscriptionStatus.ACTIVE);

        subscriptionRepository.save(sub);

        Student student = sub.getEnrollment().getStudent();

        // Enable course access only for this enrollment
        Enrollment enrollment = sub.getEnrollment();
        enrollment.setCourseAccess(true);
        enrollmentRepository.save(enrollment);

        student.setEnable(true);
        studentRepository.save(student);

        log.info("Subscription extended for student: {}. Next payment: {}. Status: {}",
                student.getEmail(),
                newNext,
                sub.getStatus());
    }

    /**
     * Disable subscription when retries finished
     */
    @Override
    public void disableSubscription(Subscription sub) {

        sub.setActive(false);

        // ✅ Set status to EXPIRED when retries are exhausted
        sub.setStatus(SubscriptionStatus.EXPIRED);

        Student student = sub.getEnrollment().getStudent();
        student.setEnable(false);

        studentRepository.save(student);
        subscriptionRepository.save(sub);

        emailService.sendAccountDisabledEmail(
                student.getEmail(),
                student.getName()
        );

        log.warn("Subscription disabled for {}. Status: {}", student.getEmail(), sub.getStatus());
    }

    /**
     * Mark subscription as PAYMENT_PENDING when sending reminders
     */
    @Override
    public void markAsPaymentPending(Subscription sub) {
        sub.setStatus(SubscriptionStatus.PAYMENT_PENDING);
        subscriptionRepository.save(sub);

        log.info("Subscription reminder sent. Status set to PAYMENT_PENDING for student: {}",
                sub.getEnrollment().getStudent().getEmail());
    }

    /**
     * Mark subscription as PAYMENT_FAILED on webhook failure
     */
    @Override
    public void markAsPaymentFailed(Subscription sub) {
        sub.setStatus(SubscriptionStatus.PAYMENT_FAILED);
        subscriptionRepository.save(sub);

        log.info("Subscription payment failed. Status set to PAYMENT_FAILED for student: {}",
                sub.getEnrollment().getStudent().getEmail());
    }
}