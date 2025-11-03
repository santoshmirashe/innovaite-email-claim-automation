package com.sapiens.innovate.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AzureAIClientConfig {

    @Value("${azure.ai.endpoint}")
    private String endPoint;

    @Value("${azure.ai.api-key}")
    private String apiKey;

    @Bean
    public WebClient azureAIWebClient(){
        return WebClient.builder()
                .baseUrl(endPoint)
                .defaultHeader("api-key",apiKey)
                .defaultHeader("Content-Type","application/json")
                .build();
    }
}