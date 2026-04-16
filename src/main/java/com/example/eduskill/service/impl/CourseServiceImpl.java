package com.example.eduskill.service.impl;

import com.example.eduskill.entity.*;
import com.example.eduskill.helper.SubscriptionHelperService;
import com.example.eduskill.repository.CourseRepository;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.StudentRepository;
import com.example.eduskill.repository.SubscriptionRepository;
import com.example.eduskill.service.CourseService;
import com.example.eduskill.service.EmailService;
import events.CourseCompletionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final SubscriptionRepository subscriptionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscriptionHelperService subscriptionHelperService;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;
    private final StudentRepository studentRepository;

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findByDeletedFalse();
    }

    @Override
    public Course getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // ✅ FIXED
        if (Boolean.TRUE.equals(course.getDeleted())) {
            throw new RuntimeException("Course is deleted");
        }

        return course;
    }

    @Override
    public Course createCourse(Course course) {

        if (course.getEnabled() == null) {
            course.setEnabled(true);
        }

        if (course.getCompleted() == null) {
            course.setCompleted(false);
        }

        if (course.getDeleted() == null) {
            course.setDeleted(false);
        }

        // TITLE DEFAULT
        if (course.getTitle() == null || course.getTitle().isEmpty()) {
            course.setTitle(course.getCourseName());
        }

        // IMAGE DEFAULT
        if (course.getImage() == null || course.getImage().isEmpty()) {
            course.setImage("https://via.placeholder.com/400x250");
        }

        // DISCOUNT DEFAULT
        if (course.getDiscountPrice() == null) {
            course.setDiscountPrice(course.getSubscriptionFee());
        }

        // ✅ NEW DEFAULTS
        if (course.getVideoUrl() == null) {
            course.setVideoUrl("");
        }

        if (course.getPdfUrl() == null) {
            course.setPdfUrl("");
        }



        return courseRepository.save(course);
    }
    @Override
    public Course updateCourse(Long id, Course updatedCourse) {

        Course course = getCourseById(id);

        course.setCourseName(updatedCourse.getCourseName());
        course.setTitle(updatedCourse.getTitle());
        course.setImage(updatedCourse.getImage());
        course.setCourseClass(updatedCourse.getCourseClass());
        course.setJoiningFee(updatedCourse.getJoiningFee());
        course.setSubscriptionFee(updatedCourse.getSubscriptionFee());
        course.setDiscountPrice(updatedCourse.getDiscountPrice());

        course.setVideoUrl(updatedCourse.getVideoUrl());
        course.setPdfUrl(updatedCourse.getPdfUrl());

        course.setEnabled(Boolean.TRUE.equals(updatedCourse.getEnabled()));
        course.setCompleted(Boolean.TRUE.equals(updatedCourse.getCompleted()));

        return courseRepository.save(course);
    }

    @Override
    public void updateCourseStatus(Long courseId, boolean enabled, boolean completed) {
        Course course = getCourseById(courseId);

        // ✅ FIXED
        boolean wasAlreadyCompleted = Boolean.TRUE.equals(course.getCompleted());

        course.setEnabled(enabled);
        course.setCompleted(completed);

        if (!wasAlreadyCompleted && completed && course.getCompletionProcessedAt() == null) {
            course.setCompletionProcessedAt(LocalDateTime.now());
            eventPublisher.publishEvent(new CourseCompletionEvent(course));
        }

        courseRepository.save(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);

        course.setDeleted(true);
        course.setDeletedAt(LocalDateTime.now());
        course.setEnabled(false);

        courseRepository.save(course);

        List<Enrollment> enrollments = enrollmentRepository.findByCourse(course);

        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();

            emailService.sendCourseCompletionEmail(
                    student.getEmail(),
                    student.getName(),
                    course.getCourseName()
            );

            enrollment.setCourseAccess(false);
            enrollmentRepository.save(enrollment);

            List<Subscription> subs = enrollment.getSubscriptions();
            for (Subscription sub : subs) {
                sub.setActive(false);
                sub.setStatus(SubscriptionStatus.EXPIRED);
                sub.setRenewalPaymentLink(null);
            }

            subscriptionRepository.saveAll(subs);
        }
    }
}