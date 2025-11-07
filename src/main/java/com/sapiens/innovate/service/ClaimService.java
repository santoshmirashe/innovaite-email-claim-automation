package com.sapiens.innovate.service;

import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.repository.InnovaiteClaimRepository;
import com.sapiens.innovate.util.Utils;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ClaimService {

    @Value("${claim.service.url}")
    private String claimServiceUrl;

    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    protected InnovaiteClaimRepository repository;

    @Autowired
    protected EmailService emailService;

    @Autowired
    private OcrService attachmentExtractorService;

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
        List<EmailVO> unreadEmails = emailService.fetchUnreadEmails(1000);

        int successCount = 0;
        int failCount = 0;

        for (EmailVO email : unreadEmails) {
            try {
                processSingleEmail(email);
                successCount++;


            } catch (Exception e) {
                log.error("Error processing email from: {}. Error: {}",
                        email.getSenderEmailAddress(), e.getMessage(), e);
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
        InnovaiteClaim claim = new InnovaiteClaim();
        ClaimDataVO claimData = null;
        claim.setEmailContent(email.getMailBody());
        claim.setSenderEmail(email.getSenderEmailAddress());
        claim.setSuccess(false);
        claim.setStatus("PENDING");
        claim.setProcessed("PENDING");
        claim.setCreatedDate(LocalDateTime.now());
        claim.setUpdateDate(LocalDateTime.now());
        try {
            //Step-0: Extract data from email
            String combinedText = buildCombinedEmailContent(email);
            // Step 1: Extract claim details using GPT
            claimData = gptProcessor.analyzeMessage(combinedText);

            claim.setPolicyNumber(claimData.getPolicyNumber());
            claim.setClaimAmount(null != claimData.getClaimAmount() ? claimData.getClaimAmount().doubleValue():0);
            claim.setPhone(claimData.getContactPhone());
            claim.setCustomerName(claimData.getContactName());

            // Validate extracted data
            validateClaimData(claimData, email);

            // Step 2: Raise the claim
            //log.info("Raising claim for policy: {}", claimData.getPolicyNumber());
            claim.setRequest(claimData.toString());
            ClaimResponseVO claimResponse = raiseClaim(claimData);

            if (claimResponse == null || claimResponse.getClaimNumber() == null) {
                throw new RuntimeException("Claim service returned invalid response");
            }

            // Step 3: Notify customer
            String subject = "Claim Received - Reference: " + claimResponse.getClaimNumber();
            String body = buildAcknowledgmentEmail(claimData, claimResponse);
            emailService.sendEmail(email.getSenderEmailAddress(), subject, body);
            claim.setSuccess(true);
            claim.setStatus("Under Review");
            claim.setClaimNumber(claimResponse.getClaimNumber());
            claim.setResponse(claimResponse.toString());
            claim.setProcessed("PROCESSED");
            log.info("Successfully processed claim: {}", claimResponse.getClaimNumber());
        }catch(Exception exception) {
            log.error(exception.getMessage());
        }finally {
            repository.save(claim);
        }
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
            claimData.setFromEmail(email.getSenderEmailAddress());
        }
    }

    private String buildAcknowledgmentEmail(ClaimDataVO claimData, ClaimResponseVO claimResponse) {
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

    public Map<String, Long> getStatistics(){
        long totalEmailsProcessed = repository.getTotalClaims();
        long successfullyProcessedEmails = repository.getTotalSuccessClaims();
        long failedToProcessEmails = repository.getTotalFailedClaims();
        return Map.of(
                "created", totalEmailsProcessed,
                "success", successfullyProcessedEmails,
                "failed", failedToProcessEmails
        );
    }

    public String buildCombinedEmailContent(EmailVO email) {
        StringBuilder combined = new StringBuilder();

        // Add subject and body
        combined.append("Subject: ").append(Utils.nullSafe(email.getMailSubject())).append("\n\n");
        combined.append("Body:\n").append(Utils.nullSafe(email.getMailBody())).append("\n\n");

        //Add attachment text (if any)
        if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
            int index = 1;
            for (EmailVO.EmailAttachment att : email.getAttachments()) {
                combined.append("--- Attachment ").append(index++).append(": ")
                        .append(att.getFilename()).append(" ---\n");

                try {
                    String extractedText = attachmentExtractorService.extractTextFromByteStream(
                            att.getContent(), att.getFilename());
                    if (extractedText == null || extractedText.isBlank()) {
                        combined.append("[No readable text extracted]\n\n");
                    } else {
                        combined.append(extractedText.trim()).append("\n\n");
                    }
                } catch (Exception e) {
                    combined.append("[Error extracting text from attachment: ")
                            .append(e.getMessage()).append("]\n\n");
                }
            }
        }

        return combined.toString();
    }

    public InnovaiteClaim saveDetailsToDB(ClaimDataVO claimDataVO,String content){
        InnovaiteClaim innovaiteClaim = new InnovaiteClaim();
        innovaiteClaim.setEmailContent(content);
        innovaiteClaim.setSuccess(false);
        innovaiteClaim.setStatus("PENDING");
        innovaiteClaim.setProcessed("PENDING");
        innovaiteClaim.setCreatedDate(LocalDateTime.now());
        innovaiteClaim.setUpdateDate(LocalDateTime.now());
        innovaiteClaim.setPolicyNumber(claimDataVO.getPolicyNumber());
        innovaiteClaim.setClaimAmount(null != claimDataVO.getClaimAmount() ? claimDataVO.getClaimAmount().doubleValue():0);
        innovaiteClaim.setPhone(claimDataVO.getContactPhone());
        innovaiteClaim.setCustomerName(claimDataVO.getContactName());
        innovaiteClaim.setRequest(claimDataVO.toString());
        repository.save(innovaiteClaim);
        return innovaiteClaim;
    }

    public InnovaiteClaim updateClaimInDB(InnovaiteClaim innovaiteClaim ,ClaimResponseVO claimResponse){
        innovaiteClaim.setSuccess(true);
        innovaiteClaim.setStatus("Under Review");
        innovaiteClaim.setClaimNumber(claimResponse.getClaimNumber());
        innovaiteClaim.setResponse(claimResponse.toString());
        innovaiteClaim.setProcessed("PROCESSED");
        repository.save(innovaiteClaim);
        return innovaiteClaim;
    }

}