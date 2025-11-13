package com.sapiens.innovate.controller;

import com.sapiens.innovate.entity.InnovaiteClaim;
import com.sapiens.innovate.service.*;
import com.sapiens.innovate.util.AnalysisResult;
import com.sapiens.innovate.vo.ClaimDTO;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api")
@Slf4j
public class OcrController {
    @Autowired
    protected OcrService ocrService;
    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    ForensicsDispatcherService forensicsDispatcherService;

    @Autowired
    protected ClaimService claimService;
    @Autowired
    protected ClaimPatternAnalyzer claimPatternAnalyzer;

    @Autowired
    protected InnovaiteClaimService innovaiteClaimService;

    @PostMapping(value = "/ocr",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        StringBuilder returnVal = new StringBuilder();
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + file.getOriginalFilename());
            file.transferTo(convFile);
            String text = ocrService.extractTextFromFile(convFile);
            ClaimDataVO claimDataVO = gptProcessor.analyzeMessage(text);
            AnalysisResult result = forensicsDispatcherService.analyze(convFile);
            List<String> patternFindings = claimPatternAnalyzer.analyze(claimDataVO);
            patternFindings.forEach(f -> {
                result.addFinding(f, 6);
            });
            claimDataVO.setPdfAnalysisResult(result);
            return ResponseEntity.ok(claimDataVO.toString());
        } catch (Exception e) {
            returnVal.append("Failed to create claim, with error ").append(e.getMessage()).append(". Try again after sometime!!!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(returnVal.toString());
        }
    }

    @PostMapping(value = "/create-claim",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createClaim(@Valid @RequestBody ClaimDataVO claimData, BindingResult bindingResult) {
        StringBuilder returnVal = new StringBuilder();
        // Handle validation errors
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getFieldErrors().forEach(err ->
                    errors.append(err.getField()).append(": ").append(err.getDefaultMessage()).append("; ")
            );
            log.warn("Validation failed: {}", errors);
            return ResponseEntity.badRequest().body("Validation error(s): " + errors);
        }

        // Log incoming payload for debugging
        log.info("Received new manual claim: {}", claimData);

        try {
            InnovaiteClaim innovaiteClaim = innovaiteClaimService.saveDetailsToDB(claimData,claimData.toString());
            ClaimResponseVO claimResponseVO = claimService.raiseClaim(claimData);
            if(claimResponseVO.getClaimNumber()!=null){
                returnVal.append("Claim created successfully for policy: " + claimData.getPolicyNumber() +" ,Claim number : "+claimResponseVO.getClaimNumber());
                claimService.updateClaimInDB(innovaiteClaim,claimResponseVO);
            }else{
                returnVal.append("Failed to create claim, try again after sometime!!!");
            }
            return ResponseEntity.ok(returnVal.toString());
        } catch (Exception ex) {
            log.error("Error creating claim: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create claim: " + ex.getMessage());
        }
    }

    @PostMapping("/retry-claim/{id}")
    public ResponseEntity<ClaimDTO> retryClaim(@PathVariable Long id) {
        try {
            ClaimDTO updated = claimService.retryClaimProcessing(id);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
