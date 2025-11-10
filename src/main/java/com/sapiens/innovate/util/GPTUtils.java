package com.sapiens.innovate.util;


import com.fasterxml.jackson.databind.JsonNode;
import com.sapiens.innovate.vo.EmailVO;

public class GPTUtils {
    public static String getSystemPrompt() {
        return """
                You are an expert insurance claims processor AI assistant.
                Your job is to extract structured claim information from customer emails.
                
                CRITICAL RULES:
                1. Return ONLY valid JSON - no markdown, no code blocks, no explanations
                2. Extract information accurately from the email text
                3. Use null for missing fields - never make up information
                4. Parse dates in YYYY-MM-DD format. Interpret relative terms like “today”, “yesterday”, or “last Monday” based on the current date context.
                5. Extract monetary amounts as numbers without currency symbols. Since there can be several documents
                6. Be flexible with field names - customers may describe things differently
                7. Look for policy numbers in various formats (POL123, P-123456, etc.)
                8. Extract contact information from signature or email body
                
                Common field variations to look for:
                - Policy: "policy number", "policy no", "policy #", "pol", "policy id"
                - Name: "name", "my name is", "I am", "policyholder"
                - Phone: "phone", "mobile", "contact", "call me at"
                - Date: "incident date", "accident date", "on", "date of incident", "occurred on","Event date"
                - Amount: "claim amount", "damages", "cost", "estimate", "$"
                """;
    }

    public static String getFewShotExample1() {
        return """
                Email Subject: Car Accident Claim
                
                Email Body:
                Hi, I need to file a claim for my car accident.
                
                My policy number is POL-789456
                Name: Sarah Johnson
                Phone: 555-0123
                Email: sarah.j@email.com
                
                My car was hit from behind on November 1st, 2024 while stopped at a red light.
                The damages are estimated at $4,500.
                
                Thanks,
                Sarah
                """;
    }

    public static String getFewShotResponse1() {
        return """
                {
                  "policyNumber": "POL-789456",
                  "policyHolderName": "Sarah Johnson",
                  "contactNumber": "555-0123",
                  "email": "sarah.j@email.com",
                  "sumInsured": null,
                  "claimedAmount": 4500,
                  "incidentDate": "2024-11-01",
                  "description": "Car was hit from behind while stopped at a red light"
                }
                """;
    }

    public static String getFewShotExample2() {
        return """
                Subject: Need help with insurance claim
                
                Hello,
                
                I'm writing to submit a claim. My policy is P123456.
                I'm John Smith and you can reach me at john@example.com or 555-9876.
                
                On October 15, 2024, there was a fire in my kitchen that caused significant damage.
                Initial estimates put the repair costs around $12,000.
                
                Please let me know what documentation you need.
                
                Best regards,
                John
                """;
    }

    public static String getFewShotResponse2() {
        return """
                {
                  "policyNumber": "P123456",
                  "policyHolderName": "John Smith",
                  "contactNumber": "555-9876",
                  "email": "john@example.com",
                  "sumInsured": null,
                  "claimedAmount": 12000,
                  "incidentDate": "2024-10-15",
                  "description": "Fire in kitchen that caused significant damage"
                }
                """;
    }

    public static String getFewShotExample3() {
        return """
                Subject: Reporting Claim Against
                                
                Hi Team,
                
                I would like to file an insurance claim for my car accident that happened yesterday. Below are my details:
            
                Policy Number: P123456
                Policy Holder Name: John Smith
                Contact Number: 555-9876
                Email: john.fernandes@example.com
                Amount to claim: ₹12000
                Event Date: 4th of November this year.
                
                My house caught fire because of a flood that occurred recently.
                Please process my claim as soon as possible.
                                
                Best regards,
                John
                """;
    }

    public static String getFewShotResponse3() {
        return """
                {
                  "policyNumber": "P123456",
                  "policyHolderName": "John Smith",
                  "contactNumber": "555-9876",
                  "email": "john@example.com",
                  "sumInsured": null,
                  "claimedAmount": 12000,
                  "incidentDate": "2025-11-04",
                  "description": " My house caught fire because of a flood that occurred recently."
                }
                """;
    }

    public static String getText(JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }

    public static String buildEnhancedPrompt(EmailVO message) {
        return String.format("""
                Extract claim information from the following email.
                Return ONLY valid JSON with these exact keys (use null for missing fields):
                
                Required fields:
                - policyNumber (string) - Look for policy number, policy ID, or any reference number
                - policyHolderName (string) - Customer's full name
                - contactNumber (string) - Phone number in any format
                - email (string) - Email address
                - incidentDate (string) - Date in YYYY-MM-DD format
                - claimedAmount (number or null) - Claimed/estimated damage amount
                - description (string) - Brief summary of the incident
                
                Optional fields:
                - sumInsured (number or null) - Total insured amount if mentioned
                
                Email Subject: %s
                
                Email Body:
                %s
                
                JSON Response:""",
                message.getMailSubject() != null ? message.getMailSubject() : "No Subject",
                message.getMailBody() != null ? message.getMailBody() : "Empty Body"
        );
    }

    public static String buildEnhancedPromptFromCombinedText(String combinedText) {
        return String.format("""
                Extract claim information from the following text which contains data from both email and attachments.
                Return ONLY valid JSON with these exact keys (use null for missing fields):
                
                Required fields:
                - policyNumber (string) - Look for policy number, policy ID, or any reference number
                - policyHolderName (string) - Customer's full name
                - contactNumber (string) - Phone number in any format
                - email (string) - Email address
                - incidentDate (string) - Date in YYYY-MM-DD format
                - claimedAmount (number or null) - Claimed/estimated damage amount
                - description (string) - Brief summary of the incident
                
                Optional:
                - sumInsured (number)
                
                Input Text:
                %s

                JSON Response:
                """, combinedText);
    }

    /**
     * Clean JSON response by removing markdown code blocks
     */
    public static String cleanJsonResponse(String response) {
        if (response == null || response.isEmpty()) {
            return "{}";
        }

        String cleaned = response.trim();

        // Remove markdown code blocks
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }
}
