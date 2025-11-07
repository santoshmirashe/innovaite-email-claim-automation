package com.sapiens.innovate.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

@Service
@Slf4j
public class OcrService {
    public String extractTextFromFile(File file) throws Exception {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf") || fileName.endsWith(".docx")) {
            String text = extractWithTika(file);
            if (text.trim().isEmpty()) {
                StringBuilder str = new StringBuilder();
                PDDocument document = PDDocument.load(file);
                PDFRenderer renderer = new PDFRenderer(document);
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("C:\\software\\Tesseract-OCR\\tessdata");
                tesseract.setLanguage("eng");
                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    BufferedImage image = renderer.renderImageWithDPI(i, 300);
                    str.append(tesseract.doOCR(image));
                }
                document.close();
                return str.toString();
            }
            return text;
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            return extractWithTesseract(file);
        } else {
            throw new IllegalArgumentException("Unsupported file type: " + fileName);
        }
    }

    public String extractTextFromByteStream(byte[] content, String filename) {
        Tika tika = new Tika();
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

    private String extractWithTika(File file) throws Exception {
        try (InputStream stream = new FileInputStream(file)) {
            org.apache.tika.parser.AutoDetectParser parser = new org.apache.tika.parser.AutoDetectParser();
            org.apache.tika.metadata.Metadata metadata = new org.apache.tika.metadata.Metadata();
            org.apache.tika.sax.BodyContentHandler handler = new org.apache.tika.sax.BodyContentHandler(-1);
            parser.parse(stream, handler, metadata, new org.apache.tika.parser.ParseContext());
            return handler.toString();
        }
    }

    private String extractWithTesseract(File file) throws Exception {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("C:\\software\\Tesseract-OCR\\tessdata"); // path to tessdata
        tesseract.setLanguage("eng");
        return tesseract.doOCR(file);
    }

    private String extractTextFromImage(byte[] imageBytes) {
        ITesseract tesseract = new Tesseract();
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

