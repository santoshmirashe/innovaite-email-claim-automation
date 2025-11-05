package com.sapiens.innovate.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SampleService {


    @Value("${azure.ai.api-key}")
    private String apiKey;

    @Value("${azure.ai.endpoint}")
    private String endPoint;

    @Value("${azure.ai.deployment}")
    private String deploymentName;


    public void runSampleService() {

        OpenAIClient client = new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endPoint)
                .buildClient();

        List<ChatRequestMessage> chatMessages = Arrays.asList(
                new ChatRequestSystemMessage("You are a helpful assistant."),
                new ChatRequestUserMessage("I am going to Paris, what should I see?")
        );

        OpenAiChatOptions openAiChatOptions = new OpenAiChatOptions();
        openAiChatOptions.setMaxCompletionTokens(16384);

        ChatCompletions chatCompletions = client.getChatCompletions(deploymentName, new ChatCompletionsOptions(chatMessages));

        System.out.printf("Model ID=%s is created at %s.%n", chatCompletions.getId(), chatCompletions.getCreatedAt());
        for (ChatChoice choice : chatCompletions.getChoices()) {
            ChatResponseMessage message = choice.getMessage();
            System.out.printf("Index: %d, Chat Role: %s.%n", choice.getIndex(), message.getRole());
            System.out.println("Message:");
            System.out.println(message.getContent());
        }
    }

    public String getAIResponse(String userMessage) {
            OpenAIClient client = new OpenAIClientBuilder()
                    .credential(new AzureKeyCredential(apiKey))
                    .endpoint(endPoint)
                    .buildClient();

            // Create chat messages (system prompt + dynamic user message)
            List<ChatRequestMessage> chatMessages = Arrays.asList(
                    new ChatRequestSystemMessage("You are a helpful assistant."),
                    new ChatRequestUserMessage(userMessage)
            );

            // Optional: configure completion settings
            OpenAiChatOptions chatOptions = new OpenAiChatOptions();
            chatOptions.setMaxCompletionTokens(2048);

            ChatCompletions chatCompletions = client.getChatCompletions(
                    deploymentName,
                    new ChatCompletionsOptions(chatMessages)
            );

            StringBuilder responseBuilder = new StringBuilder();

            for (ChatChoice choice : chatCompletions.getChoices()) {
                ChatResponseMessage message = choice.getMessage();
                if (message != null && message.getContent() != null) {
                    responseBuilder.append(message.getContent()).append("\n");
                }
            }

            return responseBuilder.toString().trim();
        }
}
