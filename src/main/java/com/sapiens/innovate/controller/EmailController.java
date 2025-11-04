package com.sapiens.innovate.controller;


import com.sapiens.innovate.service.ClaimService;
import com.sapiens.innovate.service.GmailService;
import com.sapiens.innovate.service.SampleService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
public class EmailController {
    @Autowired
    private ClaimService claimService;
    @Autowired
    private GmailService emailService;

    @Autowired
    private SampleService sampleService;


    @GetMapping("/process-mails")
    public String processEmails() throws MessagingException, IOException {
        return claimService.processClaims();
    }


    @GetMapping("/run-ai-service")
    public void runAIService(){
        sampleService.runSampleService();
    }
}
