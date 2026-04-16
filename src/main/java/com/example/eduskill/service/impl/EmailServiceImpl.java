package com.example.eduskill.service.impl;

import com.example.eduskill.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value("${subscription.test-mode:true}")
    private boolean testMode;

    // ===================== Utilities =====================
    private String formatNextPayment(LocalDateTime nextPaymentDate) {
        if (testMode) {
            Duration duration = Duration.between(LocalDateTime.now(), nextPaymentDate);
            if (duration.isNegative()) return "Now";

            long minutes = duration.toMinutes();
            long seconds = duration.minusMinutes(minutes).getSeconds();
            return String.format("in %d minutes %d seconds", minutes, seconds);
        } else {
            return nextPaymentDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
        }
    }

    // ===================== Reminder Email =====================
    @Override
    public void sendReminderEmail(String to, String studentName, LocalDateTime nextPayment) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Payment Reminder");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            String nextPaymentFriendly = formatNextPayment(nextPayment);
            helper.setText(buildReminderTemplate(studentName, nextPaymentFriendly), true);

            javaMailSender.send(message);
            log.info("Reminder email sent to {}", to);

        } catch (Exception e) {
            log.error("Reminder email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildReminderTemplate(String studentName, String nextPaymentDate) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px">
                <h2 style="color:#f39c12;text-align:center">Payment Reminder</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>This is a reminder that your EduSkill subscription payment is due <strong>%s</strong>.</p>
                <p>Please make the payment to avoid interruption of your course access.</p>
                <p style="margin-top:20px">Thank you for learning with <strong>EduSkill</strong>.</p>
            </div>
        </div>
        """.formatted(studentName, nextPaymentDate);
    }

    // ===================== Course Registration Email =====================
    @Override
    public void sendCourseRegistrationEmail(String to, String studentName, String studentCode,
                                            String courseName, LocalDateTime nextPaymentDate) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Course Registration Confirmation");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            String nextPaymentFriendly = formatNextPayment(nextPaymentDate);
            helper.setText(buildCourseRegistrationTemplate(studentName, studentCode, courseName, nextPaymentFriendly), true);

            javaMailSender.send(message);
            log.info("Course registration email sent to {}", to);

        } catch (Exception e) {
            log.error("Course registration email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildCourseRegistrationTemplate(String studentName, String studentCode,
                                                   String courseName, String nextPaymentDate) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 0 15px rgba(0,0,0,0.1)">
                <h2 style="color:#2E86C1;text-align:center">EduSkill Course Registration</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>Congratulations! Your course registration has been successfully completed.</p>
                <table style="width:100%%;border-collapse:collapse;margin-top:20px">
                    <tr><td style="border:1px solid #ddd;padding:10px"><b>Student Code</b></td><td style="border:1px solid #ddd;padding:10px">%s</td></tr>
                    <tr><td style="border:1px solid #ddd;padding:10px"><b>Course</b></td><td style="border:1px solid #ddd;padding:10px">%s</td></tr>
                    <tr><td style="border:1px solid #ddd;padding:10px"><b>Next Payment</b></td><td style="border:1px solid #ddd;padding:10px">%s</td></tr>
                </table>
                <p style="margin-top:25px">Thank you for choosing <strong>EduSkill</strong>! We wish you a great learning journey.</p>
            </div>
        </div>
        """.formatted(studentName, studentCode, courseName, nextPaymentDate);
    }

    // ===================== Subscription Renewal =====================
    @Override
    public void sendSubscriptionRenewalReminderEmail(String to, String studentName,
                                                     String courseName, LocalDateTime nextPaymentDate, String paymentLink) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Subscription Renewal Reminder");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            String nextPaymentFriendly = formatNextPayment(nextPaymentDate);
            helper.setText(buildSubscriptionRenewalTemplate(studentName, courseName, nextPaymentFriendly, paymentLink), true);

            javaMailSender.send(message);
            log.info("Subscription renewal reminder email sent to {}", to);

        } catch (Exception e) {
            log.error("Subscription renewal reminder email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildSubscriptionRenewalTemplate(String studentName, String courseName,
                                                    String nextPaymentDate, String paymentLink) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 0 15px rgba(0,0,0,0.1)">
                <h2 style="color:#2E86C1;text-align:center">EduSkill Subscription Renewal Reminder</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>Your subscription for <strong>%s</strong> will expire %s.</p>
                <div style="text-align:center;margin:20px 0">
                    <a href="%s" style="background:#2E86C1;color:white;padding:15px 30px;border-radius:5px;text-decoration:none;font-size:16px;display:inline-block">Renew Now</a>
                </div>
            </div>
        </div>
        """.formatted(studentName, courseName, nextPaymentDate, paymentLink);
    }

    // ===================== Subscription Success =====================
    @Override
    public void sendSubscriptionSuccessEmail(String to, String studentName,
                                             String courseName, LocalDateTime nextPaymentDate) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Subscription Payment Successful");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            String nextPaymentFriendly = formatNextPayment(nextPaymentDate);
            helper.setText(buildSubscriptionSuccessTemplate(studentName, courseName, nextPaymentFriendly), true);

            javaMailSender.send(message);
            log.info("Subscription success email sent to {}", to);

        } catch (Exception e) {
            log.error("Subscription success email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildSubscriptionSuccessTemplate(String studentName, String courseName, String nextPaymentDate) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 0 15px rgba(0,0,0,0.1)">
                <h2 style="color:#28a745;text-align:center;margin-bottom:30px">Subscription Payment Successful!</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>Your subscription for <strong>%s</strong> has been successfully renewed.</p>
                <table style="width:100%%;border-collapse:collapse;margin-top:20px">
                    <tr><td style="border:1px solid #ddd;padding:10px"><b>Course</b></td><td style="border:1px solid #ddd;padding:10px">%s</td></tr>
                    <tr><td style="border:1px solid #ddd;padding:10px"><b>Next Payment</b></td><td style="border:1px solid #ddd;padding:10px">%s</td></tr>
                </table>
                <p style="margin-top:20px">Thank you for continuing your learning journey with <strong>EduSkill</strong>.</p>
            </div>
        </div>
        """.formatted(studentName, courseName, courseName, nextPaymentDate);
    }

    // ===================== Course Completion =====================
    @Override
    public void sendCourseCompletionEmail(String to, String studentName, String courseName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("🎉 Congratulations! Course Completed");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            helper.setText(buildCourseCompletionTemplate(studentName, courseName), true);

            javaMailSender.send(message);
            log.info("Course completion email sent to {}", to);

        } catch (Exception e) {
            log.error("Course completion email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildCourseCompletionTemplate(String studentName, String courseName) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px">
                <h2 style="color:#28a745;text-align:center">🎉 Congratulations!</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>You have successfully completed the course:</p>
                <h3 style="color:#2E86C1">%s</h3>
                <p>We are proud of your achievement and wish you success in your future learning journey.</p>
                <p>Keep learning with <strong>EduSkilliQ Futuretech Privte Limited</strong> </p>
            </div>
        </div>
        """.formatted(studentName, courseName);
    }

    // ===================== Account Disabled =====================
    @Override
    public void sendAccountDisabledEmail(String to, String studentName) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Account Disabled");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            helper.setText(buildAccountDisabledTemplate(studentName), true);
            javaMailSender.send(message);
            log.info("Account disabled email sent to {}", to);

        } catch (Exception e) {
            log.error("Account disabled email failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildAccountDisabledTemplate(String studentName) {
        return """
        <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
            <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px">
                <h2 style="color:#dc3545;text-align:center">Account Disabled</h2>
                <p>Hello <strong>%s</strong>,</p>
                <p>Your EduSkill account has been temporarily disabled because the subscription was not renewed.</p>
                <p>If you wish to continue learning, please contact our support team to reactivate your account.</p>
                <div style="text-align:center;margin-top:20px">
                    <a href="mailto:eduskilliqfuturetech@gmail.com"
                       style="background:#2E86C1;color:white;padding:12px 25px;border-radius:5px;text-decoration:none">
                       Contact Support
                    </a>
                </div>
                <p style="margin-top:20px">We look forward to seeing you again!</p>
            </div>
        </div>
        """.formatted(studentName);
    }

    // ===================== Invoice Email =====================
    @Override
    public void sendInvoiceEmail(String to, String studentName, byte[] invoice) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Payment Invoice");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            helper.setText(
                    "Hello " + studentName +
                            ",\n\nYour payment was successful.\n" +
                            "Please find your invoice attached.\n\n" +
                            "Thank you for choosing EduSkill."
            );

            helper.addAttachment("invoice.pdf", new ByteArrayResource(invoice));

            javaMailSender.send(message);
            log.info("Invoice email sent to {}", to);

        } catch (Exception e) {
            log.error("Invoice email sending failed", e);
            throw new RuntimeException("Email sending failed", e);
        }
    }

    // ===================== Payment Failed Email =====================
    // ===================== Payment Failed Email with Payment Link =====================
    @Override
    public void sendPaymentFailedEmail(String to, String studentName, LocalDateTime nextPaymentDate, String paymentLink) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("EduSkill Payment Failed");
            helper.setFrom("eduskilliqfuturetech@gmail.com");

            String nextPaymentFriendly = formatNextPayment(nextPaymentDate);
            helper.setText(buildPaymentFailedTemplate(studentName, nextPaymentFriendly, paymentLink), true);

            javaMailSender.send(message);
            log.info("Payment failed email sent to {}", to);

        } catch (Exception e) {
            log.error("Payment failed email sending failed", e);
            throw new RuntimeException(e);
        }
    }

    private String buildPaymentFailedTemplate(String studentName, String nextPaymentDate, String paymentLink) {
        return """
    <div style="font-family:Arial,sans-serif;padding:20px;background:#f4f6f8">
        <div style="max-width:600px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 0 15px rgba(0,0,0,0.1)">
            <h2 style="color:#dc3545;text-align:center">Payment Failed</h2>
            <p>Hello <strong>%s</strong>,</p>
            <p>We were unable to process your subscription payment. Your next payment is due %s.</p>
            <p>Please click the button below to retry payment and continue your course access without interruption.</p>
            <div style="text-align:center;margin-top:20px">
                <a href="%s" style="background:#dc3545;color:white;padding:12px 25px;border-radius:5px;text-decoration:none">Retry Payment</a>
            </div>
            <p style="margin-top:20px">Thank you for learning with <strong>EduSkill</strong>.</p>
        </div>
    </div>
    """.formatted(studentName, nextPaymentDate, paymentLink);
    }
}