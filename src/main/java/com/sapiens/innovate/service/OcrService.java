package com.sapiens.innovate.service;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

    public Map<String, Object> detectPdfEditing(File file) {
        Map<String, Object> result = new HashMap<>();
        try (PDDocument doc = PDDocument.load(file)) {
            PDDocumentInformation info = doc.getDocumentInformation();
            String creator = info.getCreator();
            String producer = info.getProducer();
            String created = info.getCreationDate() != null ? info.getCreationDate().toString() : null;
            String modified = info.getModificationDate() != null ? info.getModificationDate().toString() : null;

            boolean edited = false;
            StringBuilder reason = new StringBuilder();
            // 1. Modified != Created → definitely edited
            if (created != null && modified != null && !created.equals(modified)) {
                edited = true;
                reason.append("Modification date differs from creation date. ");
            }

            // 2. Known suspicious PDF editors
            if (producer != null && (
                    producer.contains("CamScanner") ||
                            producer.contains("ILovePDF") ||
                            producer.contains("Smallpdf") ||
                            producer.contains("Foxit") ||
                            producer.contains("PDFChef") ||
                            producer.contains("Photoshop")
            )) {
                edited = true;
                reason.append("Suspicious PDF editing tool detected: ").append(producer).append(". ");
            }

            // 3. Missing or blank metadata
            if ((creator == null || creator.isBlank()) &&
                    (producer == null || producer.isBlank()) &&
                    (created == null)) {
                edited = true;
                reason.append("PDF metadata missing/cleaned (common sign of editing). ");
            }

            result.put("edited", edited);
            result.put("reason", edited ? reason.toString().trim() : "No editing detected.");
            result.put("creator", creator);
            result.put("producer", producer);
            result.put("created", created);
            result.put("modified", modified);

        } catch (Exception e) {
            result.put("edited", true);
            result.put("reason", "Unable to read PDF — likely corrupted or modified.");
        }
        return result;
    }
}