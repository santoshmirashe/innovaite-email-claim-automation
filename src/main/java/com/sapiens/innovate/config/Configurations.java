package com.sapiens.innovate.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
        try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
            properties.load(input);

            // Map properties to fields
            serverPort = Integer.parseInt(properties.getProperty("server.port"));
            applicationName = properties.getProperty("spring.application.name");

            aiApiKey = properties.getProperty("spring.ai.openai.api-key");
            aiApiUrl = properties.getProperty("spring.ai.openai.api-url");

            securityUserName = properties.getProperty("spring.security.user.name");
            securityUserPassword = properties.getProperty("spring.security.user.password");

            mailHost = properties.getProperty("spring.mail.host");
            mailPort = Integer.parseInt(properties.getProperty("spring.mail.port"));
            mailUsername = properties.getProperty("spring.mail.username");
            mailPassword = properties.getProperty("spring.mail.password");
            mailSmtpAuth = Boolean.parseBoolean(properties.getProperty("spring.mail.properties.mail.smtp.auth"));
            mailStarttlsEnable = Boolean.parseBoolean(properties.getProperty("spring.mail.properties.mail.smtp.starttls.enable"));

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    // --- Getters ---
    public int getServerPort() { return serverPort; }
    public String getApplicationName() { return applicationName; }

    public String getAiApiKey() { return aiApiKey; }
    public String getAiApiUrl() { return aiApiUrl; }

    public String getSecurityUserName() { return securityUserName; }
    public String getSecurityUserPassword() { return securityUserPassword; }

    public String getMailHost() { return mailHost; }
    public int getMailPort() { return mailPort; }
    public String getMailUsername() { return mailUsername; }
    public String getMailPassword() { return mailPassword; }
    public boolean isMailSmtpAuth() { return mailSmtpAuth; }
    public boolean isMailStarttlsEnable() { return mailStarttlsEnable; }
}
