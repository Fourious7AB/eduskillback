package com.example.eduskill.listeners;

import com.example.eduskill.entity.*;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.SubscriptionRepository;
import com.example.eduskill.service.EmailService;
import events.CourseCompletionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseCompletionListener {

    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final EmailService emailService;

    @EventListener
    @Transactional
    public void handleCourseCompletion(CourseCompletionEvent event) {
        Course course = event.getCourse();
        log.info("Processing course completion for course {}", course.getCourseName());

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();

            // Send completion email
            emailService.sendCourseCompletionEmail(
                    student.getEmail(),
                    student.getName(),
                    course.getCourseName()
            );

            // Disable access
            enrollment.setCourseAccess(false);
            enrollmentRepository.save(enrollment);

            // Expire subscriptions
            List<Subscription> subs = enrollment.getSubscriptions();
            for (Subscription sub : subs) {
                sub.setActive(false);
                sub.setStatus(SubscriptionStatus.EXPIRED);
                sub.setRenewalPaymentLink(null);
                subscriptionRepository.save(sub);
            }

            log.info("Processed student {} for course completion", student.getEmail());
        }

        log.info("Course completion processing finished for {}", course.getCourseName());
    }
}