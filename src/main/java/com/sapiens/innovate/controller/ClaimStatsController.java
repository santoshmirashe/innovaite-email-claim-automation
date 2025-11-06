package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ClaimStatsController {
    @Autowired
    private ClaimService claimService;

    @GetMapping("/api/claim-stats")
    public Map<String, Long> getClaimStats() {
        return claimService.getStatistics();
    }
}
