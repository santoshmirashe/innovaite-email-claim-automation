package com.sapiens.innovate.util;


import java.math.BigDecimal;
import java.time.LocalDate;
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

    public static LocalDate parseLocalDate(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            // Try common date formats AI might return
            return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e1) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (DateTimeParseException e2) {
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                } catch (DateTimeParseException e3) {
                    return null;
                }
            }
        }
    }

    public static String nullSafe(String val) {
        return val == null ? "" : val;
    }
}
