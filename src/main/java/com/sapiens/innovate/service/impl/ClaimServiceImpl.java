package com.sapiens.innovate.service.impl;

import com.sapiens.innovate.service.inf.ClaimService;
import com.sapiens.innovate.service.inf.EmailService;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;

import java.util.List;

public class ClaimServiceImpl implements ClaimService {

    private static ClaimService instance;

    public static synchronized ClaimService getInstance() {
        if (instance == null) {
            instance = new ClaimServiceImpl();
        }
        return instance;
    }
    @Override
    public void raiseClaim(ClaimDataVO claimDataVO) {
    //Add logic to call IDIT API
    }

    @Override
    public String processClaims() {
        EmailServiceImpl emailService = (EmailServiceImpl) EmailServiceImpl.getInstance();
        GPTProcessorImpl gptProcessor = (GPTProcessorImpl) GPTProcessorImpl.getInstance();
        List<EmailVO>  mails = emailService.getAllEmails();
        mails.forEach(mail ->{
            try {
                ClaimDataVO claimDataVO = gptProcessor.analyzeMessage(mail);
                this.raiseClaim(claimDataVO);
                emailService.markMessageRead(mail.getMessageID());
                //Add send email logic
            } catch (Exception e) {
                System.out.println("Error : "+e.getMessage());
            }
        });
        return null;
    }
}
