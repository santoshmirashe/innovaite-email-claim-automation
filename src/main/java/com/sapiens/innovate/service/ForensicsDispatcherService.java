package com.sapiens.innovate.service;

import com.sapiens.innovate.util.AnalysisResult;
import com.sapiens.innovate.util.FileTypeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class ForensicsDispatcherService {

    @Autowired
    private PdfForensicsService pdfService;

    @Autowired
    private ImageForensicsService imageService;

    public AnalysisResult analyze(File file) {

        // File not found
        if (file == null || !file.exists()) {
            return new AnalysisResult(true, 5,
                    List.of("File does not exist"), "unknown");
        }

        // PDF Check
        if (FileTypeUtil.isPdf(file)) {
            var pdf = pdfService.evaluatePdf(file);

            return new AnalysisResult(
                    pdf.isEdited(),
                    pdf.getFraudScore(),
                    pdf.getFindings(),
                    "pdf"
            );
        }

        // Image Check
        if (FileTypeUtil.isImage(file)) {
            var img = imageService.analyze(file);

            return new AnalysisResult(
                    img.isEdited(),
                    img.getFraudScore(),
                    img.getFindings(),
                    "image"
            );
        }

        // Not PDF and not Image
        AnalysisResult res = new AnalysisResult();
        res.setEdited(true);
        res.setFraudScore(5);
        res.setFindings(List.of("Not a PDF or image file"));
        res.setFileType("unknown");

        return res;
    }
}

