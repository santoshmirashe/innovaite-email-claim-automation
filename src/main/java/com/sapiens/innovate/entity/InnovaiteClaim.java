package com.sapiens.innovate.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "INNOVAITE_CLAIMS")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Getter
@Setter
public class InnovaiteClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CLAIM_NUMBER", unique = true)
    private String claimNumber;

    @Column(name = "EMAIL_CONTENT")
    private String emailContent;

    @Column(name = "POLICY_NUMBER", nullable = false)
    private String policyNumber;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @Column(name = "SENDER_EMAIL")
    private String senderEmail;

    @Column(name = "PHONE")
    private String phone;

    @Column(name = "CLAIM_AMOUNT")
    private BigDecimal claimAmount;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "EVENT_DATE", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "CREATED_DATE", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "UPDATE_DATE", nullable = false)
    private LocalDateTime updateDate;

    @Column(name = "PROCESSED")
    private String processed;

    @Column(name = "REQUEST", length = 4000)
    private String request;

    @Column(name = "RESPONSE", length = 4000)
    private String response;

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    @Column(name = "SUCCESS", nullable = false)
    private Boolean success;

    @Column(name = "EVENT_DESC")
    private String eventDesc;

    @Column(name = "FRAUD_ANALYSIS", columnDefinition = "NVARCHAR(MAX)")
    private String fraudAnalysis;

    @Column(name = "IS_EMAIL")
    private Boolean isEmail = false;
}