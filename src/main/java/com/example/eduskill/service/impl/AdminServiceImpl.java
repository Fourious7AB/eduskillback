package com.example.eduskill.service.impl;

import com.example.eduskill.entity.*;
import com.example.eduskill.helper.UserHelper;
import com.example.eduskill.repository.EnrollmentRepository;
import com.example.eduskill.repository.StudentRepository;
import com.example.eduskill.repository.SubscriptionRepository;
import com.example.eduskill.repository.UserRepository;
import com.example.eduskill.service.AdminService;

import com.example.eduskill.service.EmployeeSalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionRepository subscriptionRepository;


    @Value("${subscription.test-mode:true}")
    private boolean testMode;

    @Override
    public void disableUser(String employeeCode) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(false);
        user.setEnable(false);

        userRepository.save(user);
    }

    @Override
    public void enableUser(String employeeCode) {

        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        user.setEnable(true);

        userRepository.save(user);
    }

    @Override
    public void deleteUser(String employeeCode) {
        User user = userRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }
    @Override
    public void reactivateStudent(String studentCode) {

        // ✅ USE IT HERE
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        // Enable student
        student.setEnable(true);
        studentRepository.save(student);

        // Get enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByStudent(student);

        for (Enrollment enrollment : enrollments) {

            enrollment.setActive(true);
            enrollment.setCourseAccess(true);
            enrollmentRepository.save(enrollment);

            Subscription sub = subscriptionRepository
                    .findByEnrollment(enrollment)
                    .orElse(null);

            if (sub != null) {
                restartSubscription(sub);
            }
        }
    }
    private void restartSubscription(Subscription sub) {

        // Reset lifecycle
        sub.setActive(true);
        sub.setStatus(SubscriptionStatus.ACTIVE);
        sub.setRetryCount(0);
        sub.setReminderSent(false);
        sub.setRenewalPaymentLink(null);

        // Reset dates
        sub.setStartDate(LocalDateTime.now());

        if (testMode) {
            sub.setNextPaymentDate(LocalDateTime.now().plusMinutes(10));
        } else {
            sub.setNextPaymentDate(LocalDateTime.now().plusDays(30));
        }

        subscriptionRepository.save(sub);

        log.info("Subscription restarted for student: {}",
                sub.getEnrollment().getStudent().getEmail());
    }




}