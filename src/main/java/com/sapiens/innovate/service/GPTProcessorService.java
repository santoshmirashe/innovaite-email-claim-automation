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
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

import static com.sapiens.innovate.util.GPTUtils.*;

@Service
@Slf4j
public class GPTProcessorService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final OpenAIClient openAIClient;

    @Value("${azure.ai.deployment}")
    private String deploymentName;

    @Value("${azure.ai.temperature:2.0}")
    private Double temperature;

    @Value("${azure.ai.max-tokens:1500}")
    private Integer maxTokens;

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
        String prompt = buildEnhancedPrompt(message);
        return processPrompt(prompt);
    }


    public ClaimDataVO analyzeMessage(String combinedTextWithBodyAndAttachments) throws Exception {
        String prompt = buildEnhancedPromptFromCombinedText(combinedTextWithBodyAndAttachments);
        return processPrompt(prompt);
    }

    private ClaimDataVO processPrompt(String prompt) throws Exception {
        // Create the chat message sequence with few-shot examples
        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage(getSystemPrompt()),
                new ChatRequestUserMessage(getFewShotExample1()),
                new ChatRequestAssistantMessage(getFewShotResponse1()),
                new ChatRequestUserMessage(getFewShotExample2()),
                new ChatRequestAssistantMessage(getFewShotResponse2()),
                new ChatRequestUserMessage(getFewShotExample3()),
                new ChatRequestAssistantMessage(getFewShotResponse3()),
                new ChatRequestUserMessage(getFewShotExample4()),
                new ChatRequestAssistantMessage(getFewShotResponse4()),
                new ChatRequestUserMessage(getFewShotExample4_ConflictingPolicy()),
                new ChatRequestAssistantMessage(getFewShotResponse4_ConflictingPolicy()),
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

        log.info("AI Response: {}", content);

        // Clean response
        content = cleanJsonResponse(content);

        // Parse JSON
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

}