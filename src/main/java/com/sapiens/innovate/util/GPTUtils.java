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
                5. Extract monetary amounts as numbers without currency symbols.
                6. Be flexible with field names - customers may describe things differently
                7. Look for policy numbers ONLY in a format like:- PL-ABC-132456
                8. Extract contact information from signature or email body
                9. If multiple documents are attached (e.g., multiple bills or estimates), extract relevant fields from each and combine totals appropriately. Example: "Attachment 1: $2000, Attachment 2: $3500" → claimedAmount = 5500
                10. If some attachments contain summaries or repeated information, then summarize the information.
                
                Common field variations to look for:
                - Policy: "policy number", "policy no", "policy #", "pol", "policy id"
                - Name: "name", "my name is", "I am", "policyholder"
                - Phone: "phone", "mobile", "contact", "call me at"
                - Date: "incident date", "accident date", "on", "date of incident", "occurred on","Event date"
                - Amount: "claim amount", "damages", "cost", "estimate", "$", "total amount", "amount due", "amount to be paid", "total due"
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
                You are an intelligent insurance claim processor AI agent.

                You are provided with combined text extracted from:
                • the customer's email body
                • multiple attachments (PDFs, images, invoices, estimates, bills, etc.)

                Your job is to analyze ALL provided text carefully and produce ONE unified JSON claim record.

                -------------------------------------------
                CRITICAL BEHAVIOR RULES — FOLLOW STRICTLY
                -------------------------------------------

                1. READ EVERYTHING
                   - Do NOT ignore earlier attachments.
                   - Do NOT prioritize the last attachment unless it explicitly replaces prior values.
                   - Consider ALL monetary amounts across ALL attachments.

                2. CLAIM AMOUNT AGGREGATION (VERY IMPORTANT)
                   - Identify every monetary amount across ALL attachments.
                   - Include only amounts that represent real claimable costs (e.g., hospital charges, medical bills, repair estimates, damages).
                   - Ignore:
                       • account numbers
                       • statement IDs
                       • discounts
                       • insurance adjustments
                       • negative values unless they represent deductions
                   - Sum all valid claimable amounts into “claimedAmount”.

                3. FIELD EXTRACTION PRIORITY
                   When multiple attachments contain conflicting data:
                   - Policy Number Extraction Rules:
                           • Prefer identifiers that BEGIN with “PL-” — even if followed by hyphens, digits, or slashes.
                           • Accept formats like: PL-12345, PL-HOM-001254820/00/00, PL-ABC-123/01.
                           • If multiple policy numbers appear, choose the most complete PL- based number.
                           • Only ignore a policy number if it clearly belongs to the hospital’s internal reference (e.g., HOSP-123, REF-234).
                   - Name → Prefer the sender name in email body; fallback to patient/customer in attachments.
                   - Incident Date:
                       (1) Explicit “Date of Incident”
                       (2) “Admission Date” / “Service Date”
                       (3) Earliest date representing the event (not billing date)

                4. OUTPUT RULES
                   - Return ONLY valid JSON.
                   - No markdown.
                   - Use null for missing fields.
                   - Do NOT hallucinate values.
                   - Normalize all dates to YYYY-MM-DD.

                -------------------------------------------
                RETURN JSON WITH THESE EXACT KEYS:
                -------------------------------------------

                {
                  "policyNumber": string or null,
                  "policyHolderName": string or null,
                  "contactNumber": string or null,
                  "email": string or null,
                  "incidentDate": string (YYYY-MM-DD) or null,
                  "claimedAmount": number or null,
                  "description": string or null,
                  "sumInsured": number or null
                }

                -------------------------------------------
                NOW ANALYZE THIS CONTENT:
                -------------------------------------------

                Input (email + ALL attachments combined):
                %s

                -------------------------------------------
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

    public static String getFewShotExample4() {
        return """
            === EMAIL CONTENT ===
            Subject: Hospitalization Claim Submission

            Body:
            Dear Team,
            I am submitting my health insurance claim for my recent hospital visit.
            Please review the attached medical bills.

            Regards,
            Mary Thomas
            Contact: 555-2211
            Email: mary.t@example.com
            Policy No: PL-55678

            === ATTACHMENT 1 ===
            BILL_01.pdf ---
            CITY HOSPITAL
            Patient: Mary Thomas
            Service Date: 2025-10-05
            Room Charges: $4,500
            Procedure Charges: $2,200
            Total: $6,700

            === ATTACHMENT 2 ===
            BILL_02.pdf ---
            LAB REPORT
            Patient: Mary Thomas
            Date: 2025-10-06
            Blood Test: $550
            Radiology: $1,150
            Total Payable: 1700

            === ATTACHMENT 3 ===
            SUMMARY.pdf ---
            Insurance Summary
            Policy ID: PL-55678
            Incident Date: 2025-10-05
            Claimed Total: $8,400

            """;
    }

    public static String getFewShotResponse4() {
        return """
            {
              "policyNumber": "PL-55678",
              "policyHolderName": "Mary Thomas",
              "contactNumber": "555-2211",
              "email": "mary.t@example.com",
              "incidentDate": "2025-10-05",
              "claimedAmount": 10450,
              "description": "Medical expenses from hospitalization and related tests",
              "sumInsured": null
            }
            """;
    }

    public static String getFewShotExample4_ConflictingPolicy() {
        return """
            === EMAIL CONTENT ===
            Subject: Insurance Claim Submission

            Body:
            Hello Team,

            I am submitting a claim for my recent surgery.
            My policy number is **PL-998877**.

            Please find the hospital bill and discharge summary attached.

            Regards,
            Robert King
            Contact: 555-6611
            Email: robert.k@example.com

            === ATTACHMENT 1 ===
            BILLING_STATEMENT.pdf ---
            PATIENT: Robert King
            Policy No: HOSP-112233   (Internal billing policy reference)
            Service Date: 2025-09-10
            Charges:
              • Operation Theater: $12,000
              • Anesthesia: $3,500
              • Misc: $250
            Total: $15,750

            === ATTACHMENT 2 ===
            DISCHARGE_SUMMARY.pdf ---
            Patient: Robert King
            Hospital Insurance Ref #: REF-778899  (Not actual policy)
            Policy Number: 7788-POL-TEST (System reference)
            Diagnosis: Hernia
            Date: 2025-09-09
            Charges: $80,000
            """;
    }

    public static String getFewShotResponse4_ConflictingPolicy() {
        return """
            {
              "policyNumber": "PL-998877",
              "policyHolderName": "Robert King",
              "contactNumber": "555-6611",
              "email": "robert.k@example.com",
              "incidentDate": "2025-09-09",
              "claimedAmount": 95750,
              "description": "Surgery charges including operation theater, anesthesia, and miscellaneous fees",
              "sumInsured": null
            }
            """;
    }


}
