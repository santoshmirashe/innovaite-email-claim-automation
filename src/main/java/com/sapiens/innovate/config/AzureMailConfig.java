package com.sapiens.innovate.config;

import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class AzureMailConfig {

    @Value("${azure.client.id}")
    private String clientId;

    @Value("${azure.client.secret}")
    private String clientSecret;

    @Value("${azure.tenant.id}")
    private String tenantId;

    @Bean
    @ConditionalOnProperty(name = "azure.mail.enabled", havingValue = "true")
    public ClientSecretCredential clientSecretCredential(
            @Value("${azure.tenant-id}") String tenantId,
            @Value("${azure.client-id}") String clientId,
            @Value("${azure.client-secret}") String clientSecret) {
        return new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}