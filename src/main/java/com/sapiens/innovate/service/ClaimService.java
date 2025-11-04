package com.sapiens.innovate.service;


import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import com.sapiens.innovate.vo.EmailVO;
import jakarta.mail.MessagingException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

@Service
public class ClaimService {
    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    protected GmailService gmailService;

    private final RestTemplate restTemplate = new RestTemplate();


    public ClaimResponseVO raiseClaim(ClaimDataVO claimDataVO) {
        String url = "http://10.44.188.196/v1/claimAI";
        ResponseEntity<ClaimResponseVO> response =
                restTemplate.postForEntity(url, claimDataVO, ClaimResponseVO.class);
        return response.getBody();
    }


    /**
     * Fetch unread emails, extract claim data, raise claims, and notify customers.
     */
    public String processClaims() throws MessagingException, IOException {
        List<EmailVO> unreadEmails = gmailService.fetchUnreadEmails(1000);

        for (EmailVO email : unreadEmails) {
            try {
                processSingleEmail(email);
            } catch (Exception e) {
                System.out.println("Error : "+e.getMessage());
            }
        }

        return "Processed " + unreadEmails.size() + " email(s)";
    }

    private void processSingleEmail(EmailVO email) throws Exception {

        // Step 1: Extract claim details using GPT
        ClaimDataVO claimData = gptProcessor.analyzeMessage(email);

        // Step 2: Raise the claim
        ClaimResponseVO claimResponse = raiseClaim(claimData);

        // Step 3: Notify customer
        String subject = "Claim Received - Reference: " + claimData.getPolicyNumber();
        String body = buildAcknowledgmentEmail(email, claimData, claimResponse);

        gmailService.sendEmail(email.getSenderEmailId(), subject, body);
    }

    private String buildAcknowledgmentEmail(EmailVO email, ClaimDataVO claimData, ClaimResponseVO claimResponse) {
        return String.format("""
                        Dear %s,

                        Your claim (%s) has been successfully received and is being processed.

                        Policy Number: %s
                        Claim Reference: %s

                        Thank you for reaching out to IDIT Claims.

                        Best regards,
                        P&C Claims Team
                        """,
                email.getSenderEmailId(),
                claimResponse.getClaimNumber(),
                claimData.getPolicyNumber(),
                claimResponse.getClaimNumber()
        );
    }
}
