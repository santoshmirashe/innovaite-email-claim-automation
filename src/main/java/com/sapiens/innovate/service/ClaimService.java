package com.sapiens.innovate.service;

import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import com.sapiens.innovate.vo.EmailVO;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ClaimService {

    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    protected GmailService gmailService;

    @Value("${claim.service.url}")
    private String claimServiceUrl;

/*
    @Autowired
    private FailedClaimQueueService failedClaimQueue;
*/

    private final RestTemplate restTemplate = new RestTemplate();

    public ClaimResponseVO raiseClaim(ClaimDataVO claimDataVO) {
        String url = claimServiceUrl;
        ResponseEntity<ClaimResponseVO> response =
                restTemplate.postForEntity(url, claimDataVO, ClaimResponseVO.class);
        return response.getBody();
    }

    /**
     * Fetch unread emails, extract claim data, raise claims, and notify customers.
     */
    public String processClaims() throws MessagingException, IOException {
        List<EmailVO> unreadEmails = gmailService.fetchUnreadEmails(1000);

        int successCount = 0;
        int failCount = 0;

        for (EmailVO email : unreadEmails) {
            try {
                processSingleEmail(email);
                successCount++;


            } catch (Exception e) {
                log.error("Error processing email from: {}. Error: {}",
                        email.getSenderEmailId(), e.getMessage(), e);
                failCount++;

                // Don't mark as read so it can be retried
            }
        }

        return String.format("Processed %d email(s). Success: %d, Failed: %d",
                unreadEmails.size(), successCount, failCount);
    }

    /**
     * Process a single email with error handling and retry queue
     */
    private void processSingleEmail(EmailVO email) throws Exception {
        ClaimDataVO claimData = null;

        // Step 1: Extract claim details using GPT
        claimData = gptProcessor.analyzeMessage(email);

        // Validate extracted data
        validateClaimData(claimData, email);

        // Step 2: Raise the claim
        //log.info("Raising claim for policy: {}", claimData.getPolicyNumber());
        ClaimResponseVO claimResponse = raiseClaim(claimData);

        if (claimResponse == null || claimResponse.getClaimNumber() == null) {
            throw new RuntimeException("Claim service returned invalid response");
        }

        // Step 3: Notify customer
        String subject = "Claim Received - Reference: " + claimResponse.getClaimNumber();
        String body = buildAcknowledgmentEmail(email, claimData, claimResponse);
        gmailService.sendEmail(email.getSenderEmailId(), subject, body);

        log.info("Successfully processed claim: {}", claimResponse.getClaimNumber());

    }

    /**
     * Validate extracted claim data
     */
    private void validateClaimData(ClaimDataVO claimData, EmailVO email) throws Exception {
        if (claimData == null) {
            throw new IllegalArgumentException("Failed to extract claim data from email");
        }

        if (claimData.getPolicyNumber() == null || claimData.getPolicyNumber().isEmpty()) {
            throw new IllegalArgumentException("Policy number is required but not found in email");
        }

        if (claimData.getContactName() == null || claimData.getContactName().isEmpty()) {
            throw new IllegalArgumentException("Contact name is required but not found in email");
        }

        if (claimData.getIncidentDate() == null) {
            throw new IllegalArgumentException("Incident date is required but not found in email");
        }

        // Set email if not extracted
        if (claimData.getFromEmail() == null) {
            claimData.setFromEmail(email.getSenderEmailId());
        }
    }

    private String buildAcknowledgmentEmail(EmailVO email, ClaimDataVO claimData, ClaimResponseVO claimResponse) {
        return String.format("""
                        Dear %s,

                        Your claim has been successfully received and is being processed.

                        Claim Details:
                        - Claim Number: %s
                        - Policy Number: %s
                        - Incident Date: %s
                        - Status: Under Review

                        We will keep you updated on the progress of your claim.
                        If you have any questions, please reference your claim number when contacting us.

                        Thank you for reaching out to us.

                        Best regards,
                        P&C Claims Team
                        """,
                claimData.getContactName() != null ? claimData.getContactName() : "Valued Customer",
                claimResponse.getClaimNumber(),
                claimData.getPolicyNumber(),
                claimData.getIncidentDate()
        );
    }

    private String buildErrorEmail(EmailVO email, ClaimDataVO claimData, String errorMessage) {
        return String.format("""
                        Dear Customer,

                        We received your claim submission but need additional information to process it.

                        Issue: %s

                        Please reply to this email with the following information:
                        - Policy Number (if not provided)
                        - Your Full Name
                        - Contact Phone Number
                        - Detailed Description of the Incident
                        - Date of Incident (YYYY-MM-DD format)
                        - Estimated Claim Amount

                        We apologize for any inconvenience and look forward to processing your claim.

                        Best regards,
                        P&C Claims Team
                        """,
                errorMessage
        );
    }
}