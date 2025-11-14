package com.sapiens.innovate.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisResult {
    private boolean edited;
    private int fraudScore;
    private List<String> findings = new ArrayList<>();
    private String fileType;  // pdf, image, unknown

    public void addFinding(String msg, int score) {
        edited = true;
        fraudScore += score;
        findings.add(msg);
    }
    @Override
    public String toString() {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(this);
        } catch (Exception e) {
            return "{\"error\":\"failed to convert AnalysisResult to JSON\"}";
        }
    }
}