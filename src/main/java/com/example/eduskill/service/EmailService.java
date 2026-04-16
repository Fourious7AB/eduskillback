package com.example.eduskill.service;

import java.time.LocalDateTime;

public interface EmailService {

    void sendCourseRegistrationEmail(
            String to,
            String studentName,
            String studentCode,
            String courseName,
            LocalDateTime nextPaymentDate
    );

    void sendSubscriptionRenewalReminderEmail(
            String to,
            String studentName,
            String courseName,
            LocalDateTime nextPaymentDate,
            String paymentLink
    );

     void sendSubscriptionSuccessEmail(
            String email,
            String studentName,
            String courseName,
            LocalDateTime nextPaymentDate
    );

    // NEW
    void sendCourseCompletionEmail(
            String to,
            String studentName,
            String courseName
    );
    void sendInvoiceEmail(String to,
                          String studentName,
                          byte[] invoice);

    // NEW
    void sendReminderEmail(String email, String name, LocalDateTime nextPayment);

    void sendAccountDisabledEmail(String email, String name);

    // NEW: Payment failed notification
    // NEW: Payment failed notification with payment link
    void sendPaymentFailedEmail(String to, String studentName, LocalDateTime nextPaymentDate, String paymentLink);
}