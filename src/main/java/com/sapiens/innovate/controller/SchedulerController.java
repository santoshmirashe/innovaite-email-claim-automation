package com.sapiens.innovate.controller;

import com.sapiens.innovate.scheduler.EmailPollingScheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/scheduler")
public class SchedulerController {


    @Autowired
    private EmailPollingScheduler scheduler;


    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/start")
    public ResponseEntity<?> start() {
        scheduler.startPolling();
        return ResponseEntity.ok("scheduler started");
    }


    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/stop")
    public ResponseEntity<?> stop() {
        scheduler.stopPolling();
        return ResponseEntity.ok("scheduler stopped");
    }

    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/state")
    public ResponseEntity<?> state() {
        boolean enabled = scheduler.isPollingEnabled();
        return ResponseEntity.ok(Map.of("enabled", enabled));
    }
}