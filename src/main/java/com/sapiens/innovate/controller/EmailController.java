package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.impl.ClaimServiceImpl;
import com.sapiens.innovate.service.inf.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class EmailController {

    private final ClaimService claimService;

    public EmailController() {
        this.claimService = ClaimServiceImpl.getInstance();
    }

    @GetMapping("/process-mails")
    public String processEmails() {
        return claimService.processClaims();
    }
}
