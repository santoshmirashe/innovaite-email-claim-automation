package com.sapiens.innovate.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration(proxyBeanMethods = false)
public class Configurations {
    private static Configurations instance;

    public static synchronized Configurations getInstance() {
        if (instance == null) {
            instance = new Configurations();
        }
        return instance;
    }
    private Configurations(){};
    private int serverPort;
    private String applicationName;

    private String apiKey;

    private String apiUrl;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public int getServerPort() { return serverPort; }
    public void setServerPort(int serverPort) { this.serverPort = serverPort; }

    public String getApplicationName() { return applicationName; }
    public void setApplicationName(String applicationName) { this.applicationName = applicationName; }
}
