package com.sapiens.innovate.controller;


import com.sapiens.innovate.service.ClaimService;
import com.sapiens.innovate.service.GmailService;
import com.sapiens.innovate.service.SampleService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;


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
        return "";
        //return claimService.processClaims();
    }


    @GetMapping("/run-ai-service")
    public void runAIService(){
        sampleService.runSampleService();
    }

    @PostMapping("/ask-any-thing")
    public ResponseEntity<Map<String, String>> chatWithAI(@RequestBody Map<String, String> requestBody) {
        try {
            String userMessage = requestBody.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            String aiResponse = sampleService.getAIResponse(userMessage);
            return ResponseEntity.ok(Map.of("response", aiResponse));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get AI response: " + e.getMessage()));
        }
    }
}
