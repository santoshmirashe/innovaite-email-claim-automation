package com.sapiens.innovate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sapiens.innovate.util.FlexibleLocalDateTimeDeserializer;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @JsonDeserialize(using = FlexibleLocalDateTimeDeserializer.class)
    @JsonProperty("incidentDate")
    private LocalDateTime incidentDate;

    @JsonProperty("claimAmount")
    private BigDecimal claimAmount;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("sourceEmailMessageId")
    private String sourceEmailMessageId;

    @Override
    public String toString() {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (Exception e) {
            return "{ \"error\": \"Failed to convert ClaimDataVO to JSON\", " +
                    "\"message\": \"" + e.getMessage() + "\" }";
        }
    }
}
