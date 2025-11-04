package com.sapiens.innovate.config;

import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 🚨 Disable CSRF for testing or public APIs
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/chat").permitAll()  // 👈 Allow POST /chat
                        .requestMatchers(HttpMethod.GET, "/**").permitAll()      // Allow all GETs
                        .anyRequest().authenticated()
                )
                .httpBasic(); // still allow basic auth if needed
        return http.build();
    }
}
