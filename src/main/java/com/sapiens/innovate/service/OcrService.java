package com.sapiens.innovate.service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import org.apache.tika.parser.AutoDetectParser;

@Service
public class OcrService {

    public String extractText(File file) throws Exception {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".pdf") || fileName.endsWith(".docx")) {
            String text = extractWithTika(file);
            if (text.trim().isEmpty()) {
                StringBuilder str = new StringBuilder();
                PDDocument document = PDDocument.load(file);
                PDFRenderer renderer = new PDFRenderer(document);
                Tesseract tesseract = new Tesseract();
                tesseract.setDatapath("C:\\Santosh\\softwares\\Tesseract-OCR\\tessdata");
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
        tesseract.setDatapath("C:\\Santosh\\softwares\\Tesseract-OCR\\tessdata"); // path to tessdata
        tesseract.setLanguage("eng");
        return tesseract.doOCR(file);
    }
}

