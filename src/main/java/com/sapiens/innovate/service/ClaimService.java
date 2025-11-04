package com.sapiens.innovate.service;


import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class ClaimService {
    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    protected GmailService gmailService;

    public void raiseClaim(ClaimDataVO claimDataVO) {
    //Add logic to call IDIT API
    }

    public String processClaims() throws MessagingException, IOException {
        List<EmailVO> mails = gmailService.fetchUnreadEmails(1000);
        mails.forEach(mail ->{
            try {
                ClaimDataVO claimDataVO = gptProcessor.analyzeMessage(mail);
                this.raiseClaim(claimDataVO); //TODO by Sibtain/Vijay
                gmailService.markMessageRead(mail.getMessage());

                String subject = "Claim Received - Reference: " + claimDataVO.getPolicyNumber();
                String body = String.format("""
                        Dear %s,

                        Your claim has been successfully received and is being processed.
                        Policy No: %s

                        Best regards,
                        IDIT Claims Team
                        """, mail.getSenderEmailId(),claimDataVO.getPolicyNumber());
                gmailService.sendEmail(mail.getSenderEmailId(), subject, body);

            } catch (Exception e) {
                System.out.println("Error : "+e.getMessage());
            }
        });
        return null;
    }
}
