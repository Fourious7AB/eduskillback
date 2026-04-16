package com.example.eduskill.service.impl;

import com.example.eduskill.dto.request.StudentRegistrationRequest;
import com.example.eduskill.dto.response.OrderResponse;
import com.example.eduskill.entity.*;
import com.example.eduskill.repository.*;
import com.example.eduskill.service.StudentService;
import com.example.eduskill.service.CodeGeneratorService;
import com.example.eduskill.service.SubscriptionService; // ✅ ADDED
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final RazorpayClient razorpayClient;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final OrderRepository orderRepository;
    private final StudentRepository studentRepository;
    private final CodeGeneratorService codeGeneratorService;

    // ✅ ADDED
    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;

    @Value("${razorpay.key.id}")
    private String key;


    // ===================== HELPER METHOD =====================
    private String resolveReferralCode(String code) {
        if (code == null || code.isBlank()) {
            return "OWN_ADD";
        }
        return code.trim();
    }

    @Override
    public OrderResponse registerStudent(StudentRegistrationRequest request) throws Exception {

        // ===================== 1️⃣ FIND COURSE =====================
        Course course = courseRepository.findByCourseName(request.getCourseName())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // ===================== 2️⃣ FIND OR CREATE STUDENT =====================
        Student student = studentRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    Student s = new Student();
                    s.setStudentCode(codeGeneratorService.generateStudentCode());
                    s.setName(request.getStudentName());
                    s.setEmail(request.getEmail());
                    s.setPhone(request.getPhone());
                    s.setEnable(true);
                    s.setReferralCode(resolveReferralCode(request.getReferralCode()));
                    log.info("New student created: {}", s.getEmail());
                    return studentRepository.save(s);
                });

        // Update existing student
        student.setName(request.getStudentName());
        student.setPhone(request.getPhone());
        student.setEnable(true);
        studentRepository.save(student);

        // ===================== 3️⃣ CREATE OR GET ENROLLMENT =====================
        Enrollment enrollment = enrollmentRepository.findByStudentAndCourse(student, course)
                .orElseGet(() -> {
                    Enrollment e = new Enrollment();
                    e.setStudent(student);
                    e.setCourse(course);
                    e.setEnrolledDate(LocalDateTime.now());

                    // ✅ set joining fee as enrollment price
                    e.setEnrollmentPrice(course.getJoiningFee());

                    e.setActive(true);
                    e.setCourseAccess(true);
                    e.setFirstPaymentDone(false); // 🔥 IMPORTANT

                    log.info("New enrollment created for student {} and course {}", student.getEmail(), course.getCourseName());
                    return enrollmentRepository.save(e);
                });

        // ===================== 3️⃣.1 CREATE SUBSCRIPTION =====================
        Subscription subscription = subscriptionRepository
                .findByEnrollment(enrollment)
                .orElseGet(() -> {

                    Subscription sub = new Subscription();
                    sub.setEnrollment(enrollment);

                    log.info("Creating new subscription for enrollment {}", enrollment.getId());

                    return subscriptionService.createSubscription(sub);
                });

        // ===================== 4️⃣ DETERMINE PAYMENT TYPE =====================
        boolean isFirstPayment = !enrollment.isFirstPaymentDone();

        Integer joiningFee = course.getJoiningFee();
        Integer subscriptionFee = course.getSubscriptionFee();

        if (joiningFee == null || subscriptionFee == null) {
            throw new RuntimeException("Course fees not configured properly");
        }

        int amount = isFirstPayment ? joiningFee : subscriptionFee;

        log.info("Payment type: {} | Amount: {}",
                isFirstPayment ? "FIRST_PAYMENT" : "RENEWAL",
                amount);

        // ===================== 5️⃣ CREATE RAZORPAY ORDER =====================
        JSONObject json = new JSONObject();
        json.put("amount", amount * 100);
        json.put("currency", "INR");
        json.put("receipt", "receipt_" + System.currentTimeMillis());

        JSONObject notes = new JSONObject();
        notes.put("email", student.getEmail());
        notes.put("course", course.getCourseName());
        notes.put("subscriptionId", subscription.getId());
        notes.put("paymentType", isFirstPayment ? "FIRST_PAYMENT" : "RENEWAL"); // 🔥 BONUS

        json.put("notes", notes);

        Order razorpayOrder = razorpayClient.orders.create(json);

        // ===================== 6️⃣ SAVE ORDER =====================
        PaymentOrder dbOrder = new PaymentOrder();
        dbOrder.setRazorpayOrderId(razorpayOrder.get("id"));
        dbOrder.setAmount(amount); // ✅ FIXED
        dbOrder.setOrderStatus("PENDING");
        dbOrder.setStudentName(student.getName());
        dbOrder.setEmail(student.getEmail());
        dbOrder.setPhone(student.getPhone());
        dbOrder.setCourseName(course.getCourseName());
        dbOrder.setEnrollment(enrollment);
        orderRepository.save(dbOrder);

        // ===================== 7️⃣ RETURN RESPONSE =====================
        return OrderResponse.builder()
                .orderId(razorpayOrder.get("id"))
                .amount(amount) // ✅ FIXED
                .key(key)
                .build();
    }

}