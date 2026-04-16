package com.example.eduskill.Scheduler;

import com.example.eduskill.entity.*;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.SubscriptionRepository;
import com.example.eduskill.service.EmailService;
import com.example.eduskill.service.PaymentService;
import com.example.eduskill.service.SubscriptionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionLifecycleScheduler {

    private final SubscriptionRepository subscriptionRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final SubscriptionService subscriptionService;
    private final EnrollmentRepository enrollmentRepository;

    @Value("${subscription.test-mode:true}")
    private boolean testMode;

    @Scheduled(cron = "${subscription.scheduler.cron}")
    @Transactional
    public void processSubscriptions() {
        log.info("Subscription scheduler started");

        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptions =
                subscriptionRepository.findExpiringSubscriptions(now.plusDays(1));

        for (Subscription sub : subscriptions) {
            try {
                handleReminder(sub, now);
                handleExpiration(sub, now);
            } catch (Exception e) {
                log.error("Scheduler failed for subscription {}", sub.getId(), e);
            }
        }

        log.info("Subscription scheduler finished");
    }

    // ===================== Handle Reminder =====================
    private void handleReminder(Subscription sub, LocalDateTime now) {
        if (!sub.shouldSendReminder(now, testMode) || sub.isExpired(now)) return;

        // Ensure renewal link exists
        if (sub.getRenewalPaymentLink() == null || sub.getRenewalPaymentLink().isEmpty()) {
            sub.setRenewalPaymentLink(paymentService.generateRenewalPaymentLink(sub));
        }

        Enrollment enrollment = sub.getEnrollment();
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        emailService.sendSubscriptionRenewalReminderEmail(
                student.getEmail(),
                student.getName(),
                course.getCourseName(),
                sub.getNextPaymentDate(),
                sub.getRenewalPaymentLink()
        );

        sub.setReminderSent(true);
        subscriptionRepository.save(sub);

        log.info("Reminder sent for subscription {} (course {})", sub.getId(), course.getCourseName());
    }

    // ===================== Handle Expiration & Retry =====================
    private void handleExpiration(Subscription sub, LocalDateTime now) {

        // Prevent retry if subscription already renewed
        if (sub.getStatus() == SubscriptionStatus.ACTIVE) {
            return;
        }

        if (sub.getNextPaymentDate() == null || now.isBefore(sub.getNextPaymentDate())) return;

        Enrollment enrollment = sub.getEnrollment();
        Student student = enrollment.getStudent();
        Course course = enrollment.getCourse();

        // --------------------- Course Completion ---------------------
        if (Boolean.TRUE.equals(course.getCompleted()) && enrollment.isCourseAccess()) {
            emailService.sendCourseCompletionEmail(student.getEmail(), student.getName(), course.getCourseName());

            // Disable access only for this course
            enrollment.setCourseAccess(false);
            enrollmentRepository.save(enrollment);

            log.info("Course completion email sent for {} (course {})", student.getEmail(), course.getCourseName());
            return;
        }

        int retryCount = sub.getRetryCount() == null ? 0 : sub.getRetryCount();
        LocalDateTime lastRetry = sub.getLastRetryDate();

        // Prevent frequent retries
        if (lastRetry != null) {
            long minutesSinceLastRetry = Duration.between(lastRetry, now).toMinutes();
            if (minutesSinceLastRetry < (testMode ? 3 : 1440)) return;
        }

        // --------------------- Retry Logic ---------------------
        if (retryCount < 3 && sub.isActive()) {

            if (sub.getRenewalPaymentLink() == null || sub.getRenewalPaymentLink().isEmpty()) {
                sub.setRenewalPaymentLink(paymentService.generateRenewalPaymentLink(sub));
            }

            emailService.sendSubscriptionRenewalReminderEmail(
                    student.getEmail(),
                    student.getName(),
                    course.getCourseName(),
                    sub.getNextPaymentDate(),
                    sub.getRenewalPaymentLink()
            );

            sub.setRetryCount(retryCount + 1);
            sub.setLastRetryDate(now);
            sub.setReminderSent(false);
            subscriptionRepository.save(sub);

            log.info("Retry {} for subscription {} (course {})", retryCount + 1, sub.getId(), course.getCourseName());

        } else {
            // --------------------- Disable Subscription ---------------------
            subscriptionService.disableSubscription(sub);

            // Ensure only this enrollment/course access is disabled
            enrollment.setCourseAccess(false);
            enrollmentRepository.save(enrollment);

            log.info("Subscription disabled and course access removed for enrollment {} (course {})",
                    enrollment.getId(), course.getCourseName());
        }
    }
}