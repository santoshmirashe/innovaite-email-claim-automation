package com.sapiens.innovate.scheduler;

import com.sapiens.innovate.service.ClaimService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
@ConditionalOnProperty(
        name = "email.polling.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class EmailPollingScheduler {

    @Autowired
    private ClaimService claimService;

    @Value("${email.polling.interval.ms}")
    private long pollingInterval;

    private boolean isProcessing = false;
    private int consecutiveFailures = 0;
    private static final int MAX_CONSECUTIVE_FAILURES = 5;

    /**
     * Poll emails at configured interval
     * Default: Every 5 minutes
     * Can be configured via: email.polling.interval.ms in application.properties
     */
    @Scheduled(fixedDelayString = "${email.polling.interval.ms}")
    public void pollAndProcessEmails() {
        if (isProcessing) {
            log.warn("Previous email processing still in progress, skipping this cycle");
            return;
        }

        try {
            isProcessing = true;
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            log.info("=== Starting email polling cycle at {} ===", timestamp);

            String result = claimService.processClaims();

            log.info("=== Email polling completed: {} ===", result);
            consecutiveFailures = 0; // Reset failure counter on success

        } catch (MessagingException e) {
            consecutiveFailures++;
            log.error("Messaging error during email polling (failure {}/{}): {}",
                    consecutiveFailures, MAX_CONSECUTIVE_FAILURES, e.getMessage(), e);

            if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                log.error("CRITICAL: {} consecutive failures. Email service may be down. " +
                        "Manual intervention required.", MAX_CONSECUTIVE_FAILURES);
            }

        } catch (IOException e) {
            consecutiveFailures++;
            log.error("IO error during email polling (failure {}/{}): {}",
                    consecutiveFailures, MAX_CONSECUTIVE_FAILURES, e.getMessage(), e);

        } catch (Exception e) {
            consecutiveFailures++;
            log.error("Unexpected error during email polling (failure {}/{})",
                    consecutiveFailures, MAX_CONSECUTIVE_FAILURES, e);

        } finally {
            isProcessing = false;
        }
    }
}