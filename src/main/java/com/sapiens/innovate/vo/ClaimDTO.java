package com.sapiens.innovate.vo;

import com.sapiens.innovate.util.AnalysisResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ClaimDTO {
    private String policyNumber;
    private String customerName;
    private String claimNumber;
    private LocalDateTime createdDate;
    private boolean success;

    private Long id;

    private String analysisResult;

    private Boolean isEmail;

    public ClaimDTO() {}

    public ClaimDTO(String policyNumber, String customerName, String claimNumber,
                    LocalDateTime createdDate, boolean success, Long id,String analysisResult,Boolean isEmail) {
        this.policyNumber = policyNumber;
        this.customerName = customerName;
        this.claimNumber = claimNumber;
        this.createdDate = createdDate;
        this.success = success;
        this.id = id;
        this.analysisResult=analysisResult;
        this.isEmail = isEmail;
    }

    // Getters and Setters
    public String getPolicyNumber() {
        return policyNumber;
    }

    public void setPolicyNumber(String policyNumber) {
        this.policyNumber = policyNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getAnalysisResult() {
        return analysisResult;
    }

    public void setAnalysisResult(String analysisResult) {
        this.analysisResult = analysisResult;
    }

    public Boolean getEmail() {
        return isEmail;
    }

    public void setEmail(Boolean email) {
        isEmail = email;
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
