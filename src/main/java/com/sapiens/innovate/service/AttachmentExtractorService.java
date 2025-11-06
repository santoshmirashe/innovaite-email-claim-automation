package com.sapiens.innovate.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

@Service
@Slf4j
public class AttachmentExtractorService {

    private final Tika tika = new Tika();

    /**
     * Extracts text content from an attachment in-memory.
     * @param content The file content as byte[]
     * @param filename The original filename (helps Tika guess file type)
     * @return Extracted text, or empty string if none found
     */
    public String extractTextFromAttachment(byte[] content, String filename) {
        if (content == null || content.length == 0) {
            return "";
        }

        try (InputStream stream = new ByteArrayInputStream(content)) {
            Metadata metadata = new Metadata();
            if (filename != null) {
                metadata.set(RESOURCE_NAME_KEY, filename);
            }
            String text = tika.parseToString(stream, metadata);
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            log.error("Failed to extract text from attachment {}: {}", filename, e.getMessage());
            return "";
        }
    }
}
