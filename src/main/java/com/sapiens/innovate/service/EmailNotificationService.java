package com.sapiens.innovate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    @Value("${email.notification.enabled}")
    private boolean notificationsEnabled;

    @Value("${email.notification.from}")
    private String fromEmail;

    /**
     * Send success confirmation email
     */
    public void sendSuccessEmail(String toEmail, String claimId, String policyNumber) {
        if (!notificationsEnabled) {
            log.debug("Email notifications disabled, skipping success email");
            return;
        }

        try {
            log.info("Sending success email to: {}", toEmail);

            String subject = "Claim Successfully Created - " + claimId;
            String body = buildSuccessEmailBody(claimId, policyNumber);

            // TODO: Implement actual email sending (using JavaMail, SendGrid, etc.)
            sendEmail(toEmail, subject, body);

            log.info("Success email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send success email to: {}", toEmail, e);
        }
    }

    /**
     * Send error notification email
     */
    public void sendErrorEmail(String toEmail, String errorMessage) {
        if (!notificationsEnabled) {
            log.debug("Email notifications disabled, skipping error email");
            return;
        }

        try {
            log.info("Sending error email to: {}", toEmail);

            String subject = "Claim Submission Error";
            String body = buildErrorEmailBody(errorMessage);

            sendEmail(toEmail, subject, body);

            log.info("Error email sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Failed to send error email to: {}", toEmail, e);
        }
    }

    /**
     * Build success email body
     */
    private String buildSuccessEmailBody(String claimId, String policyNumber) {
        return """
                Dear Customer,
                
                Your insurance claim has been successfully created and is now being processed.
                
                Claim Details:
                - Claim ID: %s
                - Policy Number: %s
                - Status: Under Review
                
                You will receive updates on your claim status via email.
                If you have any questions, please contact our support team.
                
                Thank you,
                Claims Processing Team
                """.formatted(claimId, policyNumber);
    }

    /**
     * Build error email body
     */
    private String buildErrorEmailBody(String errorMessage) {
        return """
                Dear Customer,
                
                We encountered an issue while processing your claim submission.
                
                Error Details:
                %s
                
                Please review your submission and try again. Make sure to include:
                - Policy Number
                - Your contact information (name, email, phone)
                - Detailed description of the incident
                - Date of the incident
                
                If you continue to experience issues, please contact our support team.
                
                Thank you,
                Claims Processing Team
                """.formatted(errorMessage);
    }

    /**
     * Send email (placeholder - implement with actual email service)
     */
    private void sendEmail(String to, String subject, String body) {
        // TODO: Implement actual email sending
        // Option 1: Use JavaMail API
        // Option 2: Use SendGrid API
        // Option 3: Use AWS SES
        // Option 4: Use any other email service

        log.debug("Email would be sent:");
        log.debug("To: {}", to);
        log.debug("From: {}", fromEmail);
        log.debug("Subject: {}", subject);
        log.debug("Body: {}", body);

        // For now, just log the email details
        // In production, replace this with actual email sending logic
    }
}
