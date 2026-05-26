package com.example.transportfirm.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;

/**
 * Centralised file validation — checks both MIME type AND magic bytes
 * so a renamed .exe cannot bypass the content-type check.
 */
public final class FileValidationUtil {

    private static final long MAX_PDF_BYTES = 10L * 1024 * 1024; // 10 MB

    private FileValidationUtil() {}

    public static void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file is empty");
        }

        if (file.getSize() > MAX_PDF_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File too large (max 10 MB)");
        }

        // MIME type check (can be spoofed by client, so we also check magic bytes)
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }

        // Magic bytes check — PDF signature is %PDF (0x25 0x50 0x44 0x46)
        try {
            byte[] header = file.getBytes();
            if (header.length < 4
                    || header[0] != 0x25  // %
                    || header[1] != 0x50  // P
                    || header[2] != 0x44  // D
                    || header[3] != 0x46) // F
            {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "File content does not match PDF format");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File read failed");
        }
    }
}
