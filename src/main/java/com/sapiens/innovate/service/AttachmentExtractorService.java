package com.sapiens.innovate.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

@Service
@Slf4j
public class AttachmentExtractorService {

    private final Tika tika = new Tika();
    private final ITesseract tesseract = new Tesseract();

    public AttachmentExtractorService() {
        // Optional: set path to tessdata if not in system path
        // tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        // Set language (English by default)
        tesseract.setLanguage("eng");
    }

    public String extractText(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            return "";
        }

        try (InputStream stream = new ByteArrayInputStream(content)) {
            Metadata metadata = new Metadata();
            if (filename != null) {
                metadata.set(RESOURCE_NAME_KEY, filename);
            }

            // Detect type with Tika first
            String mimeType = tika.detect(stream, metadata);
            log.info("Detected MIME type: {}", mimeType);

            // Reset stream for reading
            stream.reset();

            // If it's an image → use Tess4J OCR
            if (mimeType.startsWith("image/")) {
                return extractTextFromImage(content);
            }

            // Otherwise use Tika to parse normally (PDF, DOCX, etc.)
            try (InputStream tikaStream = new ByteArrayInputStream(content)) {
                String text = tika.parseToString(tikaStream, metadata);
                return text == null ? "" : text.trim();
            }

        } catch (Exception e) {
            log.error("Failed to extract text from {}: {}", filename, e.getMessage());
            return "";
        }
    }

    private String extractTextFromImage(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                log.warn("Could not decode image for OCR.");
                return "";
            }
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            log.error("Tesseract OCR error: {}", e.getMessage());
            return "";
        } catch (Exception e) {
            log.error("Error reading image: {}", e.getMessage());
            return "";
        }
    }
}