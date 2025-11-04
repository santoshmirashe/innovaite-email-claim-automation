package com.sapiens.innovate;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.sapiens.innovate")
@EnableScheduling
public class InnovaiteEmailClaimAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.run(InnovaiteEmailClaimAutomationApplication.class, args);
	}
}
