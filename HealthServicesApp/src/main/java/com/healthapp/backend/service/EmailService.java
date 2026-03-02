package com.healthapp.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Email service using SendGrid Web API for transactional emails.
 * All methods are asynchronous (@Async) to prevent blocking API requests during email sending.
 */
@Service
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sendgrid.api.key:}")
    private String sendgridApiKey;

    @Value("${app.email.from:health.services.platform@gmail.com}")
    private String fromEmail;

    /**
     * Core method for sending emails via SendGrid Web API v3.
     * Uses HTTP POST request with Bearer authentication instead of SMTP.
     */
    private void sendSendGridEmail(String to, String subject, String htmlContent) {
        try {
            if (sendgridApiKey == null || sendgridApiKey.isEmpty()) {
                System.err.println("❌ SENDGRID_API_KEY not configured!");
                return;
            }

            String url = "https://api.sendgrid.com/v3/mail/send";

            // Set up authentication headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendgridApiKey); // Bearer token authentication

            // Build SendGrid API payload
            Map<String, Object> personalization = new HashMap<>();
            personalization.put("to", List.of(Map.of("email", to)));

            Map<String, Object> emailData = new HashMap<>();
            emailData.put("personalizations", List.of(personalization));
            emailData.put("from", Map.of("email", fromEmail));
            emailData.put("subject", subject);
            emailData.put("content", List.of(Map.of("type", "text/html", "value", htmlContent)));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(emailData, headers);

            // Send email via SendGrid API
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            System.out.println("✅ Email sent successfully to: " + to);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email via SendGrid: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Sends email verification token to newly registered users.
     * Email displays the token prominently for users to copy/paste (no clickable links).
     */
    @Async
    public void sendVerificationEmail(String toEmail, String token) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #4CAF50; text-align: center;'>Welcome to HealthApp!</h2>" +
                "<p style='font-size: 16px;'>Please verify your email address by entering the verification code below:</p>" +
                "<div style='background-color: #f0f0f0; padding: 20px; margin: 20px 0; border-radius: 4px; text-align: center;'>" +
                "<p style='margin: 0; color: #666; font-size: 14px;'>Your Verification Code</p>" +
                "<p style='margin: 10px 0; font-size: 32px; font-weight: bold; color: #4CAF50; letter-spacing: 2px; font-family: monospace;'>" + token + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Copy this code and paste it in the app to verify your account.</p>" +
                "<p style='color: #999; font-size: 12px;'>This code will expire in 24 hours.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Email Verification - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
        }
    }

    /**
     * Sends password reset token to users who requested password reset.
     * Token is displayed in the email for users to enter in the app.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String token) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #FF9800; text-align: center;'>Password Reset Request</h2>" +
                "<p style='font-size: 16px;'>You requested to reset your password. Use the code below to reset it:</p>" +
                "<div style='background-color: #fff3e0; padding: 20px; margin: 20px 0; border-radius: 4px; text-align: center; border: 2px solid #FF9800;'>" +
                "<p style='margin: 0; color: #666; font-size: 14px;'>Your Reset Code</p>" +
                "<p style='margin: 10px 0; font-size: 32px; font-weight: bold; color: #FF9800; letter-spacing: 2px; font-family: monospace;'>" + token + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Copy this code and paste it in the app to reset your password.</p>" +
                "<p style='color: #999; font-size: 12px;'>This code will expire in 24 hours.</p>" +
                "<p style='color: #d32f2f; font-size: 13px;'>⚠️ If you didn't request this, please ignore this email and your password will remain unchanged.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Password Reset - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send password reset email: " + e.getMessage());
        }
    }

    /**
     * Sends welcome email after successful email verification.
     * Confirms that the account is now active and ready to use.
     */
    @Async
    public void sendWelcomeEmail(String toEmail, String firstName) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
                "<h2 style='color: #4CAF50;'>Welcome to HealthApp, " + firstName + "!</h2>" +
                "<p>Your email has been verified successfully!</p>" +
                "<p>You can now login to HealthApp and start using our services.</p>" +
                "<p>Thank you for joining us!</p>" +
                "<p>Best regards,<br/>HealthApp Team</p>" +
                "</div>";

            sendSendGridEmail(toEmail, "Welcome to HealthApp!", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send welcome email: " + e.getMessage());
        }
    }

    /**
     * Sends account suspension notification email to user.
     */
    @Async
    public void sendAccountSuspendedEmail(String toEmail) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #d32f2f; text-align: center;'>Account Suspended</h2>" +
                "<p style='font-size: 16px;'>Your account has been suspended by an administrator.</p>" +
                "<p style='color: #666; font-size: 14px;'>You will not be able to log in until your account is reactivated.</p>" +
                "<p style='color: #666; font-size: 14px;'>If you believe this is a mistake, please contact support.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Account Suspended - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send account suspended email: " + e.getMessage());
        }
    }

    /**
     * Sends account activation notification email to user.
     */
    @Async
    public void sendAccountActivatedEmail(String toEmail) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #4CAF50; text-align: center;'>Account Activated</h2>" +
                "<p style='font-size: 16px;'>Your account has been reactivated by an administrator.</p>" +
                "<p style='color: #666; font-size: 14px;'>You can now log in and access all features of HealthApp.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Account Activated - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send account activated email: " + e.getMessage());
        }
    }

    /**
     * Sends doctor approval notification email.
     */
    @Async
    public void sendDoctorApprovedEmail(String toEmail, String doctorName) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #4CAF50; text-align: center;'>Doctor Application Approved!</h2>" +
                "<p style='font-size: 16px;'>Congratulations, Dr. " + doctorName + "!</p>" +
                "<p style='color: #666; font-size: 14px;'>Your doctor application has been approved. You can now accept appointments from patients.</p>" +
                "<p style='color: #666; font-size: 14px;'>Please log in to set up your availability schedule.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Doctor Application Approved - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send doctor approved email: " + e.getMessage());
        }
    }

    /**
     * Sends doctor rejection notification email.
     */
    @Async
    public void sendDoctorRejectedEmail(String toEmail, String doctorName, String reason) {
        try {
            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #d32f2f; text-align: center;'>Doctor Application Status</h2>" +
                "<p style='font-size: 16px;'>Dear Dr. " + doctorName + ",</p>" +
                "<p style='color: #666; font-size: 14px;'>We regret to inform you that your doctor application has been rejected.</p>" +
                "<div style='background-color: #ffebee; padding: 15px; margin: 20px 0; border-radius: 4px; border-left: 4px solid #d32f2f;'>" +
                "<p style='margin: 0; color: #666; font-size: 14px;'><strong>Reason:</strong> " + reason + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>If you have any questions or would like to reapply, please contact support.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Doctor Application Update - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send doctor rejected email: " + e.getMessage());
        }
    }

    /**
     * Sends appointment confirmation email to patient.
     */
    @Async
    public void sendAppointmentConfirmation(String toEmail, String patientName, String doctorName,
                                           LocalDate date, LocalTime time) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #4CAF50; text-align: center;'>Appointment Confirmed</h2>" +
                "<p style='font-size: 16px;'>Hello " + patientName + ",</p>" +
                "<p style='color: #666; font-size: 14px;'>Your appointment has been confirmed with the following details:</p>" +
                "<div style='background-color: #e8f5e9; padding: 20px; margin: 20px 0; border-radius: 4px;'>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Date:</strong> " + date.format(dateFormatter) + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Time:</strong> " + time.format(timeFormatter) + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Please arrive 10 minutes early for your appointment.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Appointment Confirmed - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send appointment confirmation email: " + e.getMessage());
        }
    }

    /**
     * Sends appointment cancellation email.
     */
    @Async
    public void sendAppointmentCancellation(String toEmail, String patientName, String doctorName,
                                           LocalDate date, LocalTime time) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #FF9800; text-align: center;'>Appointment Cancelled</h2>" +
                "<p style='font-size: 16px;'>Hello " + patientName + ",</p>" +
                "<p style='color: #666; font-size: 14px;'>Your appointment has been cancelled:</p>" +
                "<div style='background-color: #fff3e0; padding: 20px; margin: 20px 0; border-radius: 4px;'>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Date:</strong> " + date.format(dateFormatter) + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Time:</strong> " + time.format(timeFormatter) + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>You can book a new appointment at any time.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Appointment Cancelled - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send appointment cancellation email: " + e.getMessage());
        }
    }

    /**
     * Sends appointment reminder email.
     */
    @Async
    public void sendAppointmentReminder(String toEmail, String patientName, String doctorName,
                                       LocalDate date, LocalTime time, String clinicAddress) {
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

            String htmlContent =
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f5f5f5;'>" +
                "<div style='background-color: white; padding: 30px; border-radius: 8px;'>" +
                "<h2 style='color: #2196F3; text-align: center;'>Appointment Reminder</h2>" +
                "<p style='font-size: 16px;'>Hello " + patientName + ",</p>" +
                "<p style='color: #666; font-size: 14px;'>This is a reminder about your upcoming appointment:</p>" +
                "<div style='background-color: #e3f2fd; padding: 20px; margin: 20px 0; border-radius: 4px;'>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Doctor:</strong> Dr. " + doctorName + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Date:</strong> " + date.format(dateFormatter) + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Time:</strong> " + time.format(timeFormatter) + "</p>" +
                "<p style='margin: 5px 0; font-size: 14px;'><strong>Location:</strong> " + clinicAddress + "</p>" +
                "</div>" +
                "<p style='color: #666; font-size: 14px;'>Please arrive 10 minutes early. If you need to cancel or reschedule, please do so at least 24 hours in advance.</p>" +
                "<hr style='border: none; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='color: #999; font-size: 12px; text-align: center;'>Best regards,<br/>HealthApp Team</p>" +
                "</div></div>";

            sendSendGridEmail(toEmail, "Appointment Reminder - HealthApp", htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Failed to send appointment reminder email: " + e.getMessage());
        }
    }
}
