package com.sapiens.innovate.util;


import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import com.sapiens.innovate.vo.EmailVO;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class Utils {

    public static BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) return null;
            // Remove currency symbols or commas if AI includes them
            value = value.replaceAll("[^\\d.\\-]", "");
            return new BigDecimal(value);
        } catch (Exception e) {
            return null; // gracefully handle malformed numbers
        }
    }

    public static @NotNull(message = "Incident date is required") LocalDateTime parseLocalDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            // Try common date formats AI might return
            return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }

    public static String buildAcknowledgmentEmail(ClaimDataVO claimData, ClaimResponseVO claimResponse) {
        return String.format("""
                        Dear %s,

                        Your claim has been successfully received and is being processed.

                        Claim Details:
                        - Claim Number: %s
                        - Policy Number: %s
                        - Incident Date: %s
                        - Status: Under Review

                        We will keep you updated on the progress of your claim.
                        If you have any questions, please reference your claim number when contacting us.

                        Thank you for reaching out to us.

                        Best regards,
                        P&C Claims Team
                        """,
                claimData.getContactName() != null ? claimData.getContactName() : "Valued Customer",
                claimResponse.getClaimNumber(),
                claimData.getPolicyNumber(),
                claimData.getIncidentDate()
        );
    }

    public static String buildErrorEmail(EmailVO email, ClaimDataVO claimData, String errorMessage) {
        return String.format("""
                        Dear Customer,

                        We received your claim submission but need additional information to process it.

                        Issue: %s

                        Please reply to this email with the following information:
                        - Policy Number (if not provided)
                        - Your Full Name
                        - Contact Phone Number
                        - Detailed Description of the Incident
                        - Date of Incident (YYYY-MM-DD format)
                        - Estimated Claim Amount

                        We apologize for any inconvenience and look forward to processing your claim.

                        Best regards,
                        P&C Claims Team
                        """,
                errorMessage
        );
    }

    public static String nullSafe(String val) {
        return val == null ? "" : val;
    }
}
