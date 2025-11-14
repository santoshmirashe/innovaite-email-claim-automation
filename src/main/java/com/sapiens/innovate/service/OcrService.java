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
import java.io.*;
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

    public String extractTextFromByteStream(byte[] content, String filename) throws Exception {
        String returnVal = "";
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + filename);
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(content);
            }
            returnVal = this.extractTextFromFile(convFile);
        }catch(Exception e){
            returnVal = "";
        }
        return returnVal;
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
}