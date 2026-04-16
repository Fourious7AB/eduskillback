package com.example.eduskill.listeners;

import com.example.eduskill.config.AppConstant;
import com.example.eduskill.dto.request.PaymentCallbackRequest;
import com.example.eduskill.entity.*;
import com.example.eduskill.events.PaymentSuccessEvent;
import com.example.eduskill.repository.*;
import com.example.eduskill.service.EmailService;
import com.example.eduskill.service.InvoiceService;
import com.example.eduskill.service.SubscriptionService;
import com.example.eduskill.utility.InvoiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventListener {

    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final InvoiceService invoiceService;
    private final SubscriptionPaymentHistoryRepository historyRepository;

    @EventListener
    @Transactional
    public void handlePaymentSuccess(PaymentSuccessEvent event) {

        PaymentCallbackRequest request = event.getRequest();
        log.info("Payment success event received for order {}", request.getRazorpayOrderId());

        try {

            // ================= DUPLICATE PAYMENT CHECK =================
            Optional<Payment> existingPayment =
                    paymentRepository.findByRazorpayPaymentId(request.getRazorpayPaymentId());

            if (existingPayment.isPresent()) {
                log.warn("Duplicate payment ignored {}", request.getRazorpayPaymentId());
                return;
            }

            // ================= GET SUBSCRIPTION =================
            JSONObject notes = request.getNotes();

            if (notes == null || !notes.has("subscriptionId")) {
                log.warn("subscriptionId missing in payment notes");
                return;
            }

            Long subscriptionId = notes.getLong("subscriptionId");

            Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);

            if (subscription == null) {
                log.warn("Subscription not found {}", subscriptionId);
                return;
            }

            // ================= FETCH DATA =================
            Enrollment enrollment = subscription.getEnrollment();
            Student student = enrollment.getStudent();
            Course course = enrollment.getCourse();

            // Prevent payment after course completion
            if (Boolean.TRUE.equals(course.getCompleted())) {
                log.info("Skipping payment - course already completed {}", course.getCourseName());
                return;
            }

            // ================= 🔥 FIXED AMOUNT =================
            // ✅ ALWAYS use actual Razorpay paid amount
            int paidAmount = request.getAmount();

            if (paidAmount <= 0) {
                log.error("Invalid payment amount received from Razorpay");
                return;
            }

            // ================= 🔥 FIXED PAYMENT TYPE =================
            PaymentType paymentType;

            if (!enrollment.isFirstPaymentDone()) {

                paymentType = PaymentType.FIRST_PAYMENT;

                enrollment.setFirstPaymentDone(true);
                enrollmentRepository.save(enrollment);

                log.info("First payment detected for enrollment {}", enrollment.getId());

            } else {

                paymentType = PaymentType.RENEWAL;

                log.info("Renewal payment detected for enrollment {}", enrollment.getId());
            }

            // ================= EXTEND SUBSCRIPTION =================
            subscriptionService.extendSubscription(subscription);

            // ================= ENABLE ACCESS =================
            enrollment.setCourseAccess(true);
            enrollmentRepository.save(enrollment);

            // ================= SEND EMAIL =================
            emailService.sendCourseRegistrationEmail(
                    student.getEmail(),
                    student.getName(),
                    student.getStudentCode(),
                    course.getCourseName(),
                    subscription.getNextPaymentDate()
            );

            // ================= SAVE PAYMENT =================
            Payment payment = new Payment();
            payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
            payment.setRazorpayOrderId(request.getRazorpayOrderId());
            payment.setPaymentStatus(AppConstant.PAYMENT_SUCCESS);

            // ✅ FIXED
            payment.setAmount(paidAmount);
            payment.setPaymentType(paymentType);

            payment.setPaymentDate(LocalDateTime.now());
            payment.setSubscription(subscription);
            payment.setInvoiceNumber(InvoiceUtil.generateInvoiceNumber());

            paymentRepository.save(payment);

            // ================= SAVE PAYMENT HISTORY =================
            SubscriptionPaymentHistory history = new SubscriptionPaymentHistory();

            history.setSubscription(subscription);
            history.setRazorpayPaymentId(request.getRazorpayPaymentId());
            history.setAmount(paidAmount);
            history.setPaymentDate(payment.getPaymentDate());

            history.setPreviousPaymentDate(
                    historyRepository
                            .findTopBySubscriptionOrderByPaymentDateDesc(subscription)
                            .map(SubscriptionPaymentHistory::getNewNextPaymentDate)
                            .orElse(payment.getPaymentDate())
            );

            history.setNewNextPaymentDate(subscription.getNextPaymentDate());
            history.setInvoiceNumber(payment.getInvoiceNumber());
            history.setPaymentStatus(AppConstant.PAYMENT_SUCCESS);

            historyRepository.save(history);

            // ================= INVOICE + EMAIL =================
            try {
                byte[] invoice = invoiceService.generateInvoice(student, course, payment);

                emailService.sendInvoiceEmail(student.getEmail(), student.getName(), invoice);

                emailService.sendSubscriptionSuccessEmail(
                        student.getEmail(),
                        student.getName(),
                        course.getCourseName(),
                        subscription.getNextPaymentDate()
                );

            } catch (Exception e) {
                log.error("Invoice/email failed for {}: {}", student.getEmail(), e.getMessage());
            }

            log.info("Payment processed successfully for {} course {}",
                    student.getEmail(), course.getCourseName());

        } catch (Exception e) {
            log.error("Payment processing failed {}: {}", request.getRazorpayOrderId(), e.getMessage(), e);
        }
    }
}