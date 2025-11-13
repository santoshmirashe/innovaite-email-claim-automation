package com.sapiens.innovate.util;
import java.util.ArrayList;
import java.util.List;

public class PdfAnalysisResult {

    private boolean edited;
    private int fraudScore;
    private List<String> findings = new ArrayList<>();

    public void addFinding(String message, int score) {
        findings.add(message);
        fraudScore += score;
        edited = true;
    }

    public boolean isEdited() {
        return edited;
    }

    public int getFraudScore() {
        return fraudScore;
    }

    public List<String> getFindings() {
        return findings;
    }
}