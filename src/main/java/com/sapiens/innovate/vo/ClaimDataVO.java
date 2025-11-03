package com.sapiens.innovate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimDataVO {

    @NotBlank(message = "Policy number is required")
    @JsonProperty("policy_number")
    private String policyNumber;

    @NotBlank(message = "Contact name is required")
    @JsonProperty("contact_name")
    private String contactName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("from_email")
    private String fromEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    @NotBlank(message = "Claim description is required")
    @JsonProperty("claim_description")
    private String claimDescription;

    @NotNull(message = "Incident date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("incident_date")
    private LocalDate incidentDate;

    @JsonProperty("claim_amount")
    private BigDecimal claimAmount;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("source_email_message_id")
    private String sourceEmailMessageId;

}
