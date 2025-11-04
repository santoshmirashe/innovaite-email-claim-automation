package com.sapiens.innovate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapiens.innovate.util.Utils;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GPTProcessorService {

    private final WebClient azureAIWebClient;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${azure.ai.deployment}")
    private String deployment;

    @Value("${azure.ai.api-version}")
    private String apiVersion;

    @Autowired
    public GPTProcessorService(WebClient azureAIWebClient) {
        this.azureAIWebClient = azureAIWebClient;
    }


    public ClaimDataVO analyzeMessage(EmailVO message) throws Exception {
        String prompt = buildPrompt(message);

        // Call our model from foundry
        String requestBody = """
                {
                    "model": "%s",
                    "messages":[
                        {"role": "system", "content": "You are a helpful assistant that extracts insurance claim fields from an email. Return ONLY valid JSON with no markdown formatting or code blocks."},
                        {"role": "user", "content": "%s"}
                    ],
                    "temperature": 0
                }
                """.formatted(deployment,prompt);

        String response = azureAIWebClient.post()
                .uri("/openai/deployments/" + deployment + "/chat/completions?api-version=" + apiVersion)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        String jsonContent = extractContent(response);

        JsonNode node = mapper.readTree(jsonContent);
        ClaimDataVO claimData = ClaimDataVO.builder()
                .policyNumber(getText(node, "policyNumber"))
                .contactName(getText(node, "policyHolderName"))
                .contactPhone(getText(node, "contactNumber"))
                .fromEmail(getText(node, "email"))
                .claimAmount(Utils.parseBigDecimal(getText(node, "claimedAmount")))
                .incidentDate(Utils.parseLocalDate(getText(node, "incidentDate")))
                .summary(getText(node, "description"))
                .build();

        return claimData;
    }

    private String extractContent(String response) throws JsonProcessingException {
        JsonNode root = mapper.readTree(response);
        return root.path("choices").get(0).path("message").path("content").asText("{}");
    }

    private String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    private String buildPrompt(EmailVO message) {
        return """
                Extract the following fields from the email content below.
                Return ONLY a valid JSON object with these exact keys (use null for missing fields):
                - policyNumber (string)
                - policyHolderName (string)
                - contactNumber (string)
                - email (string)
                - sumInsured (number or null)
                - claimedAmount (number or null)
                - incidentDate (string in format YYYY-MM-DD or null)
                - description (string)
                
                Email Subject:
                %s
                
                Email Body:
                %s
                
                Return ONLY the JSON object, no markdown formatting, no code blocks, no explanations.
                """.formatted(
                message.getSubject() != null ? message.getSubject() : "",
                message.getMailBody() != null ? message.getMailBody() : ""
        );
    }
}