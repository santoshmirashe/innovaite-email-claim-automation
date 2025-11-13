package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.SampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ChatController {
    @Autowired
    private SampleService sampleService;
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAI(@RequestBody Map<String, String> requestBody) {
        try {
            String userMessage = requestBody.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message cannot be empty"));
            }

            String aiResponse = sampleService.getAIResponse(userMessage);
            return ResponseEntity.ok(Map.of(
                    "reply", aiResponse,
                    "bot", "Elon"
            ));


        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "reply", "I'm facing some issue, can you try again later!",
                    "bot", "Elon"
            ));
        }
    }
}

