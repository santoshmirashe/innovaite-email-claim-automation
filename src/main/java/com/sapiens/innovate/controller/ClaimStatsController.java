package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

@RestController
public class ClaimStatsController {
    @Autowired
    private ClaimService claimService;

    @GetMapping("/api/claim-stats")
    public ResponseEntity<Map<String, Long>> getClaimStats(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to) {

        // Defensive defaults
        if (from == null) from = LocalDate.now();
        if (to == null) to = LocalDate.now();

        if (from.isAfter(to)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", 0L)); // or throw IllegalArgumentException
        }

        Map<String, Long> stats = claimService.getStatistics(from, to);
        return ResponseEntity.ok(stats);
    }
}
