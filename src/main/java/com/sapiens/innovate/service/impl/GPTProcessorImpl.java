package com.sapiens.innovate.service.impl;

import com.sapiens.innovate.config.Configurations;
import com.sapiens.innovate.service.inf.GPTProcessor;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;
import org.springframework.web.reactive.function.client.WebClient;

public class GPTProcessorImpl implements GPTProcessor {
    private static GPTProcessor instance;
    private final WebClient webClient;

    public static synchronized GPTProcessor getInstance() {
        if (instance == null) {
            instance = new GPTProcessorImpl();
        }
        return instance;
    }
        private GPTProcessorImpl() {
            this.webClient = WebClient.builder()
                    //.baseUrl(Configurations.getInstance().getApiUrl())
                    .baseUrl("https://jfc-uigen-foundry.openai.azure.com/openai/responses?api-version=2025-04-01-preview")
                    .defaultHeader("Authorization", "Bearer " + "aEaQzlTEy4ZhrAJqcdpW8b2dhTxSfwbi23iO8Oqr7n4eoOAGZpxWJQQJ99BJACYeBjFXJ3w3AAAAACOGPvU7")
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }

        @Override
        public ClaimDataVO analyzeMessage(EmailVO message){
            String prompt = "Analyze this email for claim data and return a JSON object: " + message.getMailBody();

            // Build request payload
            String requestBody = """
                    {
                      "model": "gpt-5-mini",
                      "messages": [{"role": "user", "content": "%s"}],
                      "temperature": 0
                    }
                    """.formatted(prompt);

            String response = webClient.post()
                    .uri("https://jfc-uigen-foundry.openai.azure.com/openai/responses?api-version=2025-04-01-preview")
                    .header("api-key", "aEaQzlTEy4ZhrAJqcdpW8b2dhTxSfwbi23iO8Oqr7n4eoOAGZpxWJQQJ99BJACYeBjFXJ3w3AAAAACOGPvU7")
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            ClaimDataVO claimData = parseOpenAIResponse(response);

            return claimData;
        }

        private ClaimDataVO parseOpenAIResponse(String jsonResponse) {
            try {
                return new com.fasterxml.jackson.databind.ObjectMapper()
                        .readValue(jsonResponse, ClaimDataVO.class);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse OpenAI response", e);
            }
        }
}
