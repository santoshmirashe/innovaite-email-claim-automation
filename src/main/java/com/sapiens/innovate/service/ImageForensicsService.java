package com.sapiens.innovate.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.sapiens.innovate.util.AnalysisResult;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.sapiens.innovate.util.Utils.isImageMime;
import static com.sapiens.innovate.util.Utils.isRealImage;

@Service
public class ImageForensicsService {

    private static final Set<String> knownImageHashes = new HashSet<>();

    public AnalysisResult analyze(File file) {
        AnalysisResult result = new AnalysisResult();
        try {
            BufferedImage image = ImageIO.read(file);
            if (!isRealImage(file)) {
                result.addFinding("Not an image file", 5);
                return result;
            }

            // 1. MIME type validation
            if (!isImageMime(file)) {
                result.addFinding("Invalid MIME type — not an image", 5);
                return result;
            }
            if (image == null) {
                result.addFinding("Unable to read image — file is not a valid image", 5);
                return result;
            }

            checkExifMetadata(file, result);
            detectScreenshot(image, result);
            detectAIImage(image, result);
            detectRepeatedImages(file, image, result);
            detectSplicing(image, result);
            detectHistogramEdits(image, result);

            // Final classification
            addClassification(result);

        } catch (Exception e) {
            result.addFinding("Image unreadable or corrupted", 10);
        }

        return result;
    }

    // 1. EXIF metadata anomalies ----------------------------------------------

    private void checkExifMetadata(File file, AnalysisResult result) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            Directory exif = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

            if (exif == null) {
                result.addFinding("EXIF metadata missing (common in edited images)", 5);
                return;
            }

            Date date = exif.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
            if (date == null)
                result.addFinding("Camera timestamp missing (edited image)", 3);

            String software = exif.getString(ExifSubIFDDirectory.TAG_SOFTWARE);
            if (software != null && (
                    software.contains("Photoshop") ||
                            software.contains("Snapseed") ||
                            software.contains("PicsArt") ||
                            software.contains("AI") ||
                            software.contains("Canva")
            )) {
                result.addFinding("Image processed by suspicious software: " + software, 7);
            }

        } catch (Exception ignored) {}
    }

    // 2. Detect screenshot ------------------------------------------------------

    private void detectScreenshot(BufferedImage img, AnalysisResult result) {
        int w = img.getWidth();
        int h = img.getHeight();

        // Common screenshot resolutions
        if ((w % 180 == 0 && h % 180 == 0) || w == 1080 || h == 2400)
            result.addFinding("Image appears to be a screenshot", 5);
    }

    // 3. Detect AI-generated image (basic noise + patterns) ---------------------

    private void detectAIImage(BufferedImage img, AnalysisResult result) {
        long weirdNoise = 0;
        int w = img.getWidth(), h = img.getHeight();

        for (int x = 0; x < w - 1; x++) {
            for (int y = 0; y < h - 1; y++) {
                int rgb = img.getRGB(x, y) & 0xFF;
                int rgb2 = img.getRGB(x+1, y+1) & 0xFF;

                if (Math.abs(rgb - rgb2) < 2) weirdNoise++;
            }
        }

        if (weirdNoise > (w * h * 0.25))
            result.addFinding("AI-like uniform noise pattern detected", 10);
    }

    // 4. Perceptual hash for repeated image detection ---------------------------

    private void detectRepeatedImages(File file, BufferedImage img, AnalysisResult result) {
        try {
            String hash = computeImageHash(img);

            if (knownImageHashes.contains(hash)) {
                result.addFinding("Duplicate image used in earlier claim", 10);
            } else {
                knownImageHashes.add(hash);
            }

        } catch (Exception ignored) {}
    }

    private String computeImageHash(BufferedImage img) {
        int size = 32;
        BufferedImage scaled = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        scaled.getGraphics().drawImage(img, 0, 0, size, size, null);

        long total = 0;
        int[] pixels = new int[size * size];
        scaled.getRGB(0, 0, size, size, pixels, 0, size);
        for (int p : pixels) total += (p & 0xFF);

        int avg = (int) (total / pixels.length);

        StringBuilder hash = new StringBuilder();
        for (int p : pixels)
            hash.append((p & 0xFF) > avg ? "1" : "0");

        return hash.toString();
    }

    // 5. Splicing detection (sharp edges) ---------------------------------------

    private void detectSplicing(BufferedImage img, AnalysisResult result) {
        int w = img.getWidth(), h = img.getHeight();
        long edgeCount = 0;

        for (int x = 1; x < w; x++) {
            for (int y = 1; y < h; y++) {
                int rgb = img.getRGB(x, y);
                int rgb2 = img.getRGB(x-1, y-1);

                if (Math.abs(rgb - rgb2) > 15000000) edgeCount++;
            }
        }

        if (edgeCount > 8000)
            result.addFinding("Possible image splicing detected (sharp unnatural edges)", 10);
    }

    // 6. Histogram edit detection -----------------------------------------------

    private void detectHistogramEdits(BufferedImage img, AnalysisResult result) {
        int[] histogram = new int[256];
        int w = img.getWidth(), h = img.getHeight();

        for (int x = 0; x < w; x++)
            for (int y = 0; y < h; y++)
                histogram[ img.getRGB(x, y) & 0xFF ]++;

        // If too flat → likely edited
        long flatBins = Arrays.stream(histogram).filter(v -> v > (w*h*0.002)).count();

        if (flatBins > 180)
            result.addFinding("Histogram uniformity suggests heavy image editing", 7);
    }

    // 7. Final label -------------------------------------------------------------

    private void addClassification(AnalysisResult result) {
        if (result.getFraudScore() > 20)
            result.addFinding("High fraud risk", 0);
        else if (result.getFraudScore() > 10)
            result.addFinding("Suspicious image", 0);
    }
}

