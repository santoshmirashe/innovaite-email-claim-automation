package com.sapiens.innovate.controller;

import com.sapiens.innovate.service.GPTProcessorService;
import com.sapiens.innovate.service.OcrService;
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAndExtract(@RequestParam("file") MultipartFile file) {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + file.getOriginalFilename());
            file.transferTo(convFile);
            String text = ocrService.extractTextFromFile(convFile);

            return ResponseEntity.ok(text);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
