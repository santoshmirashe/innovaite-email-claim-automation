package com.sapiens.innovate.service;

import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.repository.InnovaiteClaimRepository;
import com.sapiens.innovate.vo.ClaimDataVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClaimPatternAnalyzer {
    @Autowired
    private InnovaiteClaimRepository repo;

    public List<String> analyze(ClaimDataVO claim) {

        List<String> findings = new ArrayList<>();

        List<InnovaiteClaim> history =
                repo.findHistory(claim.getFromEmail(), claim.getContactPhone());

        if (history.isEmpty()) return findings;

        // ---- 1. High frequency (last 30 days) ----
        long count30 = history.stream()
                .filter(c ->
                        c.getCreatedDate().isAfter(LocalDateTime.now().minusDays(30))
                ).count();

        if (count30 >= 3)
            findings.add("High claim frequency: " + count30 + " in last 30 days");


        // ---- 2. Repeated claim types ----
        long sameType = history.stream()
                .filter(c ->
                        safe(c.getEventDesc())
                                .equalsIgnoreCase(safe(claim.getClaimDescription()))
                ).count();

        if (sameType >= 2)
            findings.add("Repeat incident type detected");


        // ---- 3. Same incident date reused ----
        boolean sameDate = history.stream()
                .anyMatch(c ->
                        c.getEventDate().toLocalDate()
                                .equals(claim.getIncidentDate().toLocalDate())
                );

        if (sameDate)
            findings.add("Incident date reused in older claim");


        // ---- 4. Amount pattern (fraud pattern: same claimed amount repeatedly) ----
        boolean sameAmount = history.stream()
                .anyMatch(c ->
                        c.getClaimAmount() != null &&
                                c.getClaimAmount().compareTo(claim.getClaimAmount()) == 0
                );

        if (sameAmount)
            findings.add("Repeated claim amount pattern");

        return findings;
    }

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
