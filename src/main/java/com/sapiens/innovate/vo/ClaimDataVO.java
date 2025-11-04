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
    @JsonProperty("policyNumber")
    private String policyNumber;

    @NotBlank(message = "Contact name is required")
    @JsonProperty("contactName")
    private String contactName;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @JsonProperty("fromEmail")
    private String fromEmail;

    @JsonProperty("contactPhone")
    private String contactPhone;

    @NotBlank(message = "Claim description is required")
    @JsonProperty("claimDescription")
    private String claimDescription;

    @NotNull(message = "Incident date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("incidentDate")
    private LocalDate incidentDate;

    @JsonProperty("claimAmount")
    private BigDecimal claimAmount;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("sourceEmailMessageId")
    private String sourceEmailMessageId;

}
