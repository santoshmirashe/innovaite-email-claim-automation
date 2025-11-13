package com.sapiens.innovate.util;


import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import com.sapiens.innovate.vo.EmailVO;
import jakarta.validation.constraints.NotNull;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static String[] requestPatterns = new String[]{"/","/login-page", "/css/**", "/js/**", "/images/**",
            "/fragments/**","/auth/login","/homepage/**","/api/**","/register-page","/register", "/auth/register"};

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
                    try {
                        return LocalDateTime.parse(value);
                    } catch (DateTimeParseException e4) {
                        try {
                        return LocalDate.parse(value).atStartOfDay();
                        } catch (DateTimeParseException e5) {
                            return LocalDateTime.now();
                        }
                    }
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

    private static final long MAX_PDF_SIZE_MB = 15; // 15 MB limit

    public static boolean isTooLarge(File file) {
        long fileMb = file.length() / (1024 * 1024);
        return fileMb > MAX_PDF_SIZE_MB;
    }
    public static boolean isEncryptedPdf(File file) {
        try {
            PDDocument doc = PDDocument.load(file, (String) null);
            boolean encrypted = doc.isEncrypted();
            doc.close();
            return encrypted;
        } catch (Exception e) {
            return true;
        }
    }
    public static boolean isPdfMimeType(File file) {
        try {
            org.apache.tika.Tika tika = new org.apache.tika.Tika();
            String mime = tika.detect(file);
            return mime.equals("application/pdf");
        } catch (Exception e) {
            return false;
        }
    }
    public static boolean isPdfFile(File file) {
        try (java.io.FileInputStream fis = new java.io.FileInputStream(file)) {

            byte[] header = new byte[4];
            if (fis.read(header) != 4) {
                return false;
            }

            // PDF magic number check → detects real PDFs
            return header[0] == '%' &&
                    header[1] == 'P' &&
                    header[2] == 'D' &&
                    header[3] == 'F';

        } catch (Exception e) {
            return false;
        }
    }
}
