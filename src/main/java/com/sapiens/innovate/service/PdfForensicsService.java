package com.sapiens.innovate.service;

import com.sapiens.innovate.util.AnalysisResult;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import javax.imageio.ImageIO;

import static com.sapiens.innovate.util.Utils.*;

@Service
public class PdfForensicsService {

    public AnalysisResult evaluatePdf(File file) {
        AnalysisResult result = new AnalysisResult();
        // 0. Actual PDF magic number check
        if (!isPdfFile(file)) {
            result.addFinding("Not a PDF file", 5);
            return result;
        }

        // 1. MIME type check
        if (!isPdfMimeType(file)) {
            result.addFinding("Invalid MIME type — not a real PDF", 5);
            return result;
        }

        // 2. File size check
        if (isTooLarge(file)) {
            result.addFinding("File exceeds maximum PDF size limit", 5);
            return result;
        }

        // 3. Reject encrypted PDFs
        if (isEncryptedPdf(file)) {
            result.addFinding("Encrypted or password-protected PDF not allowed", 6);
            return result;
        }

        try (PDDocument doc = PDDocument.load(file)) {

            checkMetadataMismatch(doc, result);                   // 1
            checkSuspiciousProducers(doc, result);                // 2
            checkInsertedImages(doc, result);                     // 3
            checkMissingTextLayers(doc, result);                  // 4
            checkSignatureTampering(doc, result);                 // 5
            checkReExportedPdf(doc, result);                      // 6
            checkIncrementalUpdates(doc, result);                 // 7
            checkPageInsertDelete(doc, result);                   // 8
            checkImageManipulation(doc, result, file);            // 9
            addFinalScoreClassification(result);                  // 10

        } catch (Exception e) {
            result.addFinding("PDF unreadable or corrupted. Likely modified.", 10);
        }

        return result;
    }

    // --------------------------------------------------------------------------
    // 1. Metadata mismatch
    // --------------------------------------------------------------------------
    private void checkMetadataMismatch(PDDocument doc, AnalysisResult result) {
        try {
            PDDocumentInformation info = doc.getDocumentInformation();

            String created = info.getCreationDate() != null ? info.getCreationDate().toString() : null;
            String modified = info.getModificationDate() != null ? info.getModificationDate().toString() : null;

            if (created != null && modified != null && !created.equals(modified)) {
                result.addFinding("Metadata mismatch: Modified date differs from Creation date.", 5);
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 2. Suspicious PDF editors (CamScanner, ILovePDF, Photoshop, etc.)
    // --------------------------------------------------------------------------
    private void checkSuspiciousProducers(PDDocument doc, AnalysisResult result) {
        try {
            String producer = doc.getDocumentInformation().getProducer();
            if (producer == null) return;

            List<String> suspiciousTools = Arrays.asList(
                    "CamScanner", "ILovePDF", "Smallpdf", "PDFChef", "Photoshop", "Foxit"
            );

            for (String tool : suspiciousTools) {
                if (producer.contains(tool)) {
                    result.addFinding("Suspicious editing tool used: " + producer, 7);
                }
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 3. Detect inserted images (converted images instead of text)
    // --------------------------------------------------------------------------
    private void checkInsertedImages(PDDocument doc, AnalysisResult result) {
        try {
            int imageCount = 0;

            for (PDPage page : doc.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName xObjectName : resources.getXObjectNames()) {
                    if (resources.isImageXObject(xObjectName)) {
                        imageCount++;
                    }
                }
            }

            if (imageCount >= doc.getNumberOfPages()) {
                result.addFinding("PDF is image-based (likely screenshot or edited).", 6);
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 4. Missing text layers (pure image PDF)
    // --------------------------------------------------------------------------
    private void checkMissingTextLayers(PDDocument doc, AnalysisResult result) {
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(doc);

            if (text.trim().isEmpty()) {
                result.addFinding("No text layer detected — PDF likely edited or converted from image.", 5);
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 5. Digital signature tampering
    // --------------------------------------------------------------------------
    private void checkSignatureTampering(PDDocument doc, AnalysisResult result) {
        try {
            List<PDSignature> signatures = doc.getSignatureDictionaries();
            if (signatures.isEmpty()) {
                result.addFinding("No valid digital signature found.", 3);
                return;
            }

            for (PDSignature sig : signatures) {
                if (sig.getByteRange() == null) {
                    result.addFinding("Signature byte-range invalid — signature tampering detected.", 8);
                }
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 6. Detect re-exported PDF (missing metadata + image heavy)
    // --------------------------------------------------------------------------
    private void checkReExportedPdf(PDDocument doc, AnalysisResult result) {
        PDDocumentInformation info = doc.getDocumentInformation();

        if (info.getCreator() == null && info.getProducer() == null) {
            result.addFinding("PDF metadata stripped — likely re-exported after editing.", 6);
        }
    }

    // --------------------------------------------------------------------------
    // 7. Incremental updates (true edit history)
    // --------------------------------------------------------------------------
    private void checkIncrementalUpdates(PDDocument doc, AnalysisResult result) {
        try {
            COSDocument cos = doc.getDocument();
            if (cos != null && cos.getXrefTable().size() > 100) {
                result.addFinding("Large number of incremental updates — PDF was edited multiple times.", 5);
            }
        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 8. Detect pages added/removed (page size mismatch etc.)
    // --------------------------------------------------------------------------
    private void checkPageInsertDelete(PDDocument doc, AnalysisResult result) {
        Set<String> pageSizes = new HashSet<>();

        for (PDPage page : doc.getPages()) {
            pageSizes.add(page.getMediaBox().toString());
        }

        if (pageSizes.size() > 1) {
            result.addFinding("Detected inconsistent page sizes — possible page insert/delete.", 4);
        }
    }

    // --------------------------------------------------------------------------
    // 9. Detect image manipulation (pixel inconsistencies)
    // --------------------------------------------------------------------------
    private void checkImageManipulation(PDDocument doc, AnalysisResult result, File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return;

            long highContrastCount = 0;

            for (int x = 1; x < img.getWidth(); x++) {
                for (int y = 1; y < img.getHeight(); y++) {
                    int rgb1 = img.getRGB(x, y);
                    int rgb2 = img.getRGB(x - 1, y - 1);

                    if (Math.abs(rgb1 - rgb2) > 15000000) {
                        highContrastCount++;
                    }
                }
            }

            if (highContrastCount > 5000) {
                result.addFinding("Detected pixel-level manipulation in images.", 10);
            }

        } catch (Exception ignored) {}
    }

    // --------------------------------------------------------------------------
    // 10. Final scoring classification
    // --------------------------------------------------------------------------
    private void addFinalScoreClassification(AnalysisResult result) {

        int score = result.getFraudScore();

        if (score >= 20) {
            result.addFinding("High likelihood of PDF manipulation.", 0);
        } else if (score >= 10) {
            result.addFinding("Suspicious PDF — manual review recommended.", 0);
        }
    }
}

