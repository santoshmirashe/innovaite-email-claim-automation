package com.sapiens.innovate.config;

import lombok.Getter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Getter
public class Configurations {

    private static Configurations instance;
    private Properties properties;

    // --- Config fields ---
    private int serverPort;
    private String applicationName;

    private String aiApiKey;
    private String aiApiUrl;

    private String securityUserName;
    private String securityUserPassword;

    private String mailHost;
    private int mailPort;
    private String mailUsername;
    private String mailPassword;
    private boolean mailSmtpAuth;
    private boolean mailStarttlsEnable;

    // --- Private constructor ---
    private Configurations() {
        loadProperties();
    }

    // --- Singleton accessor ---
    public static synchronized Configurations getInstance() {
        if (instance == null) {
            instance = new Configurations();
        }
        return instance;
    }

    // --- Load properties from file ---
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("application.properties not found in classpath");
            }
            properties.load(input);

            // Map properties to fields
            serverPort = Integer.parseInt(properties.getProperty("server.port", "8085"));
            applicationName = properties.getProperty("spring.application.name", "DefaultApp");

            aiApiKey = properties.getProperty("spring.ai.openai.api-key");
            aiApiUrl = properties.getProperty("spring.ai.openai.api-url");

            securityUserName = properties.getProperty("spring.security.user.name");
            securityUserPassword = properties.getProperty("spring.security.user.password");

            mailHost = properties.getProperty("spring.mail.host");
            mailPort = Integer.parseInt(properties.getProperty("spring.mail.port", "587"));
            mailUsername = properties.getProperty("spring.mail.username");
            mailPassword = properties.getProperty("spring.mail.password");
            mailSmtpAuth = Boolean.parseBoolean(properties.getProperty("spring.mail.properties.mail.smtp.auth", "true"));
            mailStarttlsEnable = Boolean.parseBoolean(properties.getProperty("spring.mail.properties.mail.smtp.starttls.enable", "true"));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }
}
