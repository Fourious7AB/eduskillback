package com.example.eduskill.service.impl;

import com.example.eduskill.config.AppConstant;
import com.example.eduskill.dto.request.PaymentCallbackRequest;
import com.example.eduskill.entity.*;
import com.example.eduskill.events.PaymentSuccessEvent;
import com.example.eduskill.repository.*;
import com.example.eduskill.service.EmailService;
import com.example.eduskill.service.InvoiceService;
import com.example.eduskill.service.PaymentService;
import com.example.eduskill.service.SubscriptionService;
import com.example.eduskill.utility.InvoiceUtil;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final OrderRepository orderRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final RazorpayClient razorpayClient;
    private final ApplicationEventPublisher eventPublisher;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SubscriptionService subscriptionService;
    private final EmailService emailService;
    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;
    private final SubscriptionPaymentHistoryRepository historyRepository;
    private final WebhookEventRepository webhookEventRepository;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Value("${webhook.secret}")
    private String razorpayWebhookSecret;

    @Value("${webhook.enabled:true}")
    private boolean webhookEnabled;

    // =====================================================
    // VERIFY INITIAL COURSE PAYMENT
    // =====================================================
    @Override
    public boolean verifyPayment(PaymentCallbackRequest request) {
        try {
            String data = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            String generatedSignature = HmacUtils.hmacSha256Hex(razorpaySecret, data);

            if (!generatedSignature.equals(request.getRazorpaySignature())) {
                log.warn("Payment signature mismatch");
                return false;
            }

            PaymentOrder order = orderRepository
                    .findByRazorpayOrderId(request.getRazorpayOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));


            order.setOrderStatus("PAID");
            orderRepository.save(order);

            Enrollment enrollment = order.getEnrollment();

            if (!enrollment.isFirstPaymentDone()) {
                enrollment.setFirstPaymentDone(true);
                enrollmentRepository.save(enrollment);

                log.info("First payment completed for enrollment {}", enrollment.getId());
            }
            Subscription subscription = subscriptionRepository
                    .findByEnrollment(order.getEnrollment())
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            JSONObject notes = new JSONObject();
            notes.put("subscriptionId", subscription.getId());

            request.setNotes(notes);

            request.setStudentName(order.getStudentName());
            request.setEmail(order.getEmail());
            request.setPhone(order.getPhone());
            request.setCourseName(order.getCourseName());

            request.setAmount(order.getAmount());
            log.info("Publishing payment success event");
            eventPublisher.publishEvent(new PaymentSuccessEvent(request));

            return true;

        } catch (Exception e) {
            log.error("Payment verification failed", e);
            return false;
        }
    }

    // =====================================================
    // GENERATE RENEWAL PAYMENT LINK
    // =====================================================
    @Override
    public String generateRenewalPaymentLink(Subscription sub) {
        try {
            Student student = sub.getEnrollment().getStudent();
            Course course = sub.getEnrollment().getCourse();

            Integer subscriptionFee = course.getSubscriptionFee();

            if (subscriptionFee == null) {
                throw new RuntimeException("Subscription fee not configured for course");
            }

            int amount = subscriptionFee;

            JSONObject options = new JSONObject();
            options.put("amount", amount * 100);
            options.put("currency", "INR");
            options.put("description", "Subscription renewal for " + course.getCourseName());

            String referenceId = "SUB_" + sub.getId() + "_" + System.currentTimeMillis();
            options.put("reference_id", referenceId);

            JSONObject notes = new JSONObject();
            notes.put("subscriptionId", sub.getId());
            notes.put("type", "SUBSCRIPTION_RENEWAL");
            options.put("notes", notes);

// 🔧 sanitize phone before sending to Razorpay
            JSONObject customer = new JSONObject();
            customer.put("name", student.getName());
            customer.put("email", student.getEmail());

// sanitize phone before sending to Razorpay
            String phone = student.getPhone();

            if (phone == null || !phone.matches("^[6-9]\\d{9}$")) {
                log.warn("Invalid phone for student {}. Using fallback.", student.getEmail());
                phone = "9123456789";
            }

            customer.put("contact", phone);
            options.put("customer", customer);

            options.put("notify", new JSONObject().put("sms", true).put("email", true));
            options.put("reminder_enable", true);

            PaymentLink link = razorpayClient.paymentLink.create(options);
            String paymentUrl = link.get("short_url");

            sub.setRenewalPaymentLink(paymentUrl);
            sub.setLastRetryDate(LocalDateTime.now());
            sub.setStatus(SubscriptionStatus.PAYMENT_PENDING); // ⬅ mark pending when link generated
            subscriptionRepository.save(sub);

            log.info("Generated renewal payment link for subscription {}. Status set to PAYMENT_PENDING", sub.getId());
            return paymentUrl;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create payment link : " + e.getMessage());
        }
    }

    // =====================================================
    // PROCESS RAZORPAY WEBHOOK
    // =====================================================
    @Override
    @Transactional
    public void processWebhook(String payload, String signature) {

        if (!webhookEnabled) {
            log.warn("Webhook processing skipped: disabled in configuration");
            return;
        }

        log.info("Webhook received payload: {}", payload);

        try {
            if (signature == null || signature.isEmpty()) {
                log.error("Missing Razorpay webhook signature");
                throw new RuntimeException("Missing Razorpay webhook signature");
            }

            Utils.verifyWebhookSignature(payload, signature, razorpayWebhookSecret);
            log.info("Webhook signature verified successfully");

            JSONObject json = new JSONObject(payload);
            String eventType = json.optString("event");

            // Only process relevant payment events
            if (!"payment.captured".equals(eventType)
                    && !"payment_link.paid".equals(eventType)
                    && !"payment.failed".equals(eventType)) {
                log.info("Ignoring event type {}", eventType);
                return;
            }

            JSONObject paymentEntity = extractPaymentEntity(json);
            if (paymentEntity == null) return;


            String razorpayPaymentId = paymentEntity.optString("id");

            if (paymentRepository.findByRazorpayPaymentId(razorpayPaymentId).isPresent()) {
                log.warn("Duplicate payment webhook ignored {}", razorpayPaymentId);
                return;
            }

            JSONObject notes = paymentEntity.optJSONObject("notes");

            if (notes == null || !notes.has("subscriptionId")) {
                log.warn("subscriptionId missing in webhook notes");
                return;
            }

            Long subscriptionId = notes.getLong("subscriptionId");
            Subscription sub = subscriptionRepository
                    .findById(subscriptionId)
                    .orElse(null);

            if (sub == null) {
                log.warn("Subscription not found {}", subscriptionId);
                return;
            }

            Student student = sub.getEnrollment().getStudent();
            Course course = sub.getEnrollment().getCourse();
            // Handle payment.failed

            if ("payment.failed".equals(eventType)) {
                int retry = sub.getRetryCount() == null ? 0 : sub.getRetryCount();
                sub.setRetryCount(retry + 1);

                // Mark subscription as PAYMENT_FAILED
                subscriptionService.markAsPaymentFailed(sub);

                // ✅ Ensure renewal payment link exists
                String paymentLink = sub.getRenewalPaymentLink();
                if (paymentLink == null || paymentLink.trim().isEmpty()) {
                    paymentLink = generateRenewalPaymentLink(sub); // generate and save link
                }

                // ✅ Notify student immediately with link
                emailService.sendPaymentFailedEmail(
                        student.getEmail(),
                        student.getName(),
                        sub.getNextPaymentDate(),
                        paymentLink
                );

                subscriptionRepository.save(sub);
                log.warn("Payment failed. Retry count {} for subscription {}", sub.getRetryCount(), sub.getId());
                return;
            }
            // ===============================
// Handle successful payment
// ===============================

            String paymentTypeStr = notes.optString("paymentType", "UNKNOWN");

// ✅ SAFETY: skip if paymentType missing
            if ("UNKNOWN".equals(paymentTypeStr)) {
                log.warn("Missing paymentType in webhook. Skipping.");
                return;
            }

// ❌ IMPORTANT: Ignore FIRST PAYMENT (handled by PaymentEventListener)
            if ("FIRST_PAYMENT".equals(paymentTypeStr)) {
                log.info("Skipping FIRST_PAYMENT in webhook. Already handled separately.");
                return;
            }

// ✅ ONLY SUBSCRIPTION PAYMENTS CONTINUE

            Payment payment = new Payment();
            payment.setRazorpayPaymentId(razorpayPaymentId);
            payment.setRazorpayOrderId(paymentEntity.optString("order_id"));
            payment.setPaymentStatus(AppConstant.PAYMENT_SUCCESS);

// ✅ ALWAYS use Razorpay actual paid amount
            int paidAmount = paymentEntity.optInt("amount") / 100;
            payment.setAmount(paidAmount);

            payment.setPaymentDate(LocalDateTime.now());
            payment.setSubscription(sub);
            payment.setInvoiceNumber(InvoiceUtil.generateInvoiceNumber());

// ✅ This is guaranteed renewal now
            payment.setPaymentType(PaymentType.RENEWAL);

// ✅ SAVE PAYMENT FIRST
            paymentRepository.save(payment);

// ✅ THEN EXTEND SUBSCRIPTION
            subscriptionService.extendSubscription(sub);

// ✅ SAVE HISTORY
            SubscriptionPaymentHistory history = new SubscriptionPaymentHistory();
            history.setSubscription(sub);
            history.setRazorpayPaymentId(razorpayPaymentId);
            history.setAmount(payment.getAmount());
            history.setPaymentDate(payment.getPaymentDate());
            history.setPreviousPaymentDate(
                    historyRepository.findTopBySubscriptionOrderByPaymentDateDesc(sub)
                            .map(SubscriptionPaymentHistory::getNewNextPaymentDate)
                            .orElse(payment.getPaymentDate())
            );
            history.setNewNextPaymentDate(sub.getNextPaymentDate());
            history.setInvoiceNumber(payment.getInvoiceNumber());
            history.setPaymentStatus(AppConstant.PAYMENT_SUCCESS);

            historyRepository.save(history);

// ✅ Generate & send invoice
            byte[] invoice = invoiceService.generateInvoice(student, course, payment);
            emailService.sendInvoiceEmail(student.getEmail(), student.getName(), invoice);

            log.info("Subscription renewed via webhook for {}", student.getEmail());

            // Mark webhook as processed


        } catch (Exception e) {
            log.error("Webhook processing failed", e);
        }
    }

    // -------------------------
    // Helper: Extract payment entity
    // -------------------------
    private JSONObject extractPaymentEntity(JSONObject json) {
        JSONObject payloadObj = json.getJSONObject("payload");
        if (payloadObj.has("payment")) {
            return payloadObj.getJSONObject("payment").getJSONObject("entity");
        } else if (payloadObj.has("payment_link")) {
            return payloadObj.getJSONObject("payment_link").getJSONObject("entity");
        } else {
            log.warn("Unknown webhook payload structure");
            return null;
        }
    }
}