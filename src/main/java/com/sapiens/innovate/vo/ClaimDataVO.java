package com.sapiens.innovate.vo;

import java.time.LocalDate;

public class ClaimDataVO {
    private String policyNumber;
    private LocalDate incidentDate;
    private String claimType;
    private String summary;
    private String sourceEmailMessageId;
    private String fromEmail;
    public ClaimDataVO() {}
    public String getPolicyNumber() { return policyNumber; }
    public void setPolicyNumber(String policyNumber) { this.policyNumber = policyNumber; }
    public LocalDate getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDate incidentDate) { this.incidentDate = incidentDate; }
    public String getClaimType() { return claimType; }
    public void setClaimType(String claimType) { this.claimType = claimType; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getSourceEmailMessageId() { return sourceEmailMessageId; }
    public void setSourceEmailMessageId(String sourceEmailMessageId) { this.sourceEmailMessageId = sourceEmailMessageId; }
    public String getFromEmail() { return fromEmail; }
    public void setFromEmail(String fromEmail) { this.fromEmail = fromEmail; }
    @Override public String toString() {
        return "ClaimData{" + "policyNumber='" + policyNumber + '\'' +
                ", incidentDate=" + incidentDate +
                ", claimType='" + claimType + '\'' +
                ", summary='" + summary + '\'' +
                ", sourceEmailMessageId='" + sourceEmailMessageId + '\'' +
                ", fromEmail='" + fromEmail + '\'' + '}';
    }
}
