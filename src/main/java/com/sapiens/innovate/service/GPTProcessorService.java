package com.sapiens.innovate.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sapiens.innovate.util.Utils;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.EmailVO;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GPTProcessorService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final OpenAIClient openAIClient;

    @Value("${azure.ai.deployment}")
    private String deploymentName;

    public GPTProcessorService(
            @Value("${azure.ai.api-key}") String apiKey,
            @Value("${azure.ai.endpoint}") String endpoint
    ) {
        this.openAIClient = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildClient();
    }

    public ClaimDataVO analyzeMessage(EmailVO message) throws Exception {
        String prompt = buildPrompt(message);

        // Create the chat message sequence
        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(
                        "You are a helpful assistant that extracts insurance claim fields from an email. " +
                                "Return ONLY valid JSON with no markdown formatting or code blocks."
                ),
                new ChatRequestUserMessage(prompt)
        );


        OpenAiChatOptions options = new OpenAiChatOptions();
        options.setTemperature(0.0);
        options.setMaxCompletionTokens(1500);

        // Invoke the Azure model
        ChatCompletions chatCompletions = openAIClient.getChatCompletions(deploymentName, new ChatCompletionsOptions(chatMessages));

        // Extract response text
        String content = chatCompletions.getChoices().get(0).getMessage().getContent();
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("AI response was empty or invalid");
        }

        JsonNode node = mapper.readTree(content);
        ClaimDataVO claimData = ClaimDataVO.builder()
                .policyNumber(getText(node, "policyNumber"))
                .contactName(getText(node, "policyHolderName"))
                .contactPhone(getText(node, "contactNumber"))
                .fromEmail(getText(node, "email"))
                .claimAmount(Utils.parseBigDecimal(getText(node, "claimedAmount")))
                .incidentDate(Utils.parseLocalDate(getText(node, "incidentDate")))
                .claimDescription(getText(node, "description"))
                .build();

        return claimData;
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
