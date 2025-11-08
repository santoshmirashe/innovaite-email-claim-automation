package com.sapiens.innovate.service;

import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.repository.InnovaiteClaimRepository;
import com.sapiens.innovate.util.Utils;
import com.sapiens.innovate.vo.*;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    protected InnovaiteClaimRepository claimRepository;

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
            claim.setClaimAmount(null != claimData.getClaimAmount() ? claimData.getClaimAmount(): BigDecimal.ZERO);
            claim.setPhone(claimData.getContactPhone());
            claim.setCustomerName(claimData.getContactName());
            claim.setEventDate(claimData.getIncidentDate());

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
            String body = Utils.buildAcknowledgmentEmail(claimData, claimResponse);
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
            claimRepository.save(claim);
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

    public Map<String, Long> getStatistics(LocalDate from, LocalDate to){
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime toDateTime = (to != null) ? to.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);
        long totalEmailsProcessed = claimRepository.getTotalClaims(fromDateTime,toDateTime);
        long successfullyProcessedEmails = claimRepository.getTotalSuccessClaims(fromDateTime,toDateTime);
        long failedToProcessEmails = claimRepository.getTotalFailedClaims(fromDateTime,toDateTime);
        return Map.of(
                "created", totalEmailsProcessed,
                "success", successfullyProcessedEmails,
                "failed", failedToProcessEmails
        );
    }

    public PaginatedClaimResponse getClaimsPaginated(LocalDate from, LocalDate to, int offset, int limit) {
        LocalDateTime fromDateTime = (from != null) ? from.atStartOfDay() : LocalDate.now().atStartOfDay();
        LocalDateTime toDateTime = (to != null) ? to.atTime(LocalTime.MAX) : LocalDate.now().atTime(LocalTime.MAX);

        Pageable pageable = PageRequest.of(offset / limit, limit);
        Page<InnovaiteClaim> page = claimRepository.findClaims(fromDateTime, toDateTime, pageable);

        List<ClaimDTO> claimDTOs = page.getContent().stream()
                .map(c -> new ClaimDTO(
                        c.getPolicyNumber(),
                        c.getCustomerName(),
                        c.getClaimNumber(),
                        c.getCreatedDate(),
                        c.getSuccess()
                ))
                .toList();

        return new PaginatedClaimResponse(
                claimDTOs,
                page.getTotalElements(),
                pageable.getPageNumber(),
                pageable.getPageSize()
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


    public InnovaiteClaim updateClaimInDB(InnovaiteClaim innovaiteClaim ,ClaimResponseVO claimResponse){
        innovaiteClaim.setSuccess(true);
        innovaiteClaim.setStatus("Under Review");
        innovaiteClaim.setClaimNumber(claimResponse.getClaimNumber());
        innovaiteClaim.setResponse(claimResponse.toString());
        innovaiteClaim.setProcessed("PROCESSED");
        innovaiteClaim.setUpdateDate(LocalDateTime.now());
        claimRepository.save(innovaiteClaim);
        return innovaiteClaim;
    }

    public ClaimDTO retryClaimProcessing(String policyNumber) {
        StringBuilder returnVal = new StringBuilder();
        InnovaiteClaim claim = claimRepository.findByPolicyNumber(policyNumber)
                .orElseThrow(() -> new RuntimeException("Claim record not found in DB"));
        ClaimDataVO claimData = ClaimDataVO.builder()
                .contactName(claim.getCustomerName())
                .contactPhone(claim.getPhone())
                .policyNumber(claim.getPolicyNumber())
                .claimAmount(claim.getClaimAmount())
                .claimDescription(claim.getEventDesc())
                .incidentDate(claim.getEventDate())
                .build();

        ClaimResponseVO claimResponseVO = raiseClaim(claimData);
                // Simulate reprocessing logic (re-run OCR, GPT, etc.)
                // For demo, just update status + generate a new claim number
        if(claimResponseVO.getClaimNumber()!=null){
            returnVal.append("Claim created successfully for policy: " + claimData.getPolicyNumber() +" ,Claim number : "+claimResponseVO.getClaimNumber());
            updateClaimInDB(claim,claimResponseVO);
        }else{
            returnVal.append("Failed to create claim, try again after sometime!!!");
        }

        return new ClaimDTO(
                claim.getPolicyNumber(),
                claim.getCustomerName(),
                claim.getClaimNumber(),
                claim.getCreatedDate(),
                claim.getSuccess()
        );
    }

}