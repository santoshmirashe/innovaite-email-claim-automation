package com.sapiens.innovate.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class FileTypeUtil {

    // PDF magic number check: %PDF
    public static boolean isPdf(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[4];
            if (fis.read(header) < 4) return false;

            return header[0] == '%' &&
                    header[1] == 'P' &&
                    header[2] == 'D' &&
                    header[3] == 'F';

        } catch (Exception e) {
            return false;
        }
    }

    // IMAGE magic number check (JPG/PNG/GIF/BMP/WEBP)
    public static boolean isImage(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[12];
            if (fis.read(header) < 12) return false;

            // JPG
            if (header[0] == (byte)0xFF && header[1] == (byte)0xD8) return true;

            // PNG
            if (header[0] == (byte)0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') return true;

            // GIF
            if (header[0] == 'G' && header[1] == 'I' && header[2] == 'F') return true;

            // BMP
            if (header[0] == 'B' && header[1] == 'M') return true;

            // WEBP (RIFF....WEBP)
            if (header[0] == 'R' && header[1] == 'I' && header[2] == 'F' &&
                    header[8] == 'W' && header[9] == 'E' &&
                    header[10] == 'B' && header[11] == 'P') return true;

            return false;

        } catch (Exception e) {
            return false;
        }
    }

    public static File getFileFromStream(byte[] content, String filename){
        File convFile = new File(System.getProperty("java.io.tmpdir") + filename);
        try {
            try (FileOutputStream fos = new FileOutputStream(convFile)) {
                fos.write(content);
            }
        }catch(Exception e){
           return null;
        }
        return convFile;
    }
}

