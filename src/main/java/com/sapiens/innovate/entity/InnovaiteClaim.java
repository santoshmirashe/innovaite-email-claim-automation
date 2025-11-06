package com.sapiens.innovate.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "INNOVAITE_CLAIMS")
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
    private Double claimAmount;

    @Column(name = "STATUS", nullable = false)
    private String status;

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

    @Column(name = "SUCCESS", nullable = false)
    private Boolean success;

    // Getters and Setters
    public String getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(String emailContent) {
        this.emailContent = emailContent;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClaimNumber() { return claimNumber; }
    public void setClaimNumber(String claimNumber) { this.claimNumber = claimNumber; }

    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getSenderEmail() { return senderEmail; }
    public void setSenderEmail(String senderEmail) { this.senderEmail = senderEmail; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Double getClaimAmount() { return claimAmount; }
    public void setClaimAmount(Double claimAmount) { this.claimAmount = claimAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getUpdateDate() { return updateDate; }
    public void setUpdateDate(LocalDateTime updateDate) { this.updateDate = updateDate; }

    public String getProcessed() { return processed; }
    public void setProcessed(String processed) { this.processed = processed; }

    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
}
