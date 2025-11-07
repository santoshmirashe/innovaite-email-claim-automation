package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.ClaimService;
import com.sapiens.innovate.service.GPTProcessorService;
import com.sapiens.innovate.service.OcrService;
import com.sapiens.innovate.vo.ClaimDataVO;
import com.sapiens.innovate.vo.ClaimResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {
    @Autowired
    protected OcrService ocrService;
    @Autowired
    protected GPTProcessorService gptProcessor;

    @Autowired
    protected ClaimService claimService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        StringBuilder returnVal = new StringBuilder();
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + file.getOriginalFilename());
            file.transferTo(convFile);
            String text = ocrService.extractTextFromFile(convFile);
            ClaimDataVO claimDataVO = gptProcessor.analyzeMessage(text);
            ClaimResponseVO claimResponseVO = claimService.raiseClaim(claimDataVO);
            if(claimResponseVO.getClaimNumber()!=null){
                returnVal.append("Claim created successfully : "+claimResponseVO.getClaimNumber());
            }else{
                returnVal.append("Failed to create claim, try again after sometime!!!");
            }
            return ResponseEntity.ok(returnVal.toString());
        } catch (Exception e) {
            returnVal.append("Failed to create claim, with error ").append(e.getMessage()).append(". Try again after sometime!!!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(returnVal.toString());
        }
    }
}
