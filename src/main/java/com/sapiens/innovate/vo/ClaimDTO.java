package com.sapiens.innovate.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClaimDTO {
    private String policyNumber;
    private String customerName;
    private String claimNumber;
    private LocalDateTime createdDate;
    private boolean success;

    public ClaimDTO() {}

    public ClaimDTO(String policyNumber, String customerName, String claimNumber,
                    LocalDateTime createdDate, boolean success) {
        this.policyNumber = policyNumber;
        this.customerName = customerName;
        this.claimNumber = claimNumber;
        this.createdDate = createdDate;
        this.success = success;
    }

    // Getters and Setters
    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ClaimDTO{" +
                "policyNumber='" + policyNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", claimNumber='" + claimNumber + '\'' +
                ", createdDate=" + createdDate +
                ", success=" + success +
                '}';
    }
}
