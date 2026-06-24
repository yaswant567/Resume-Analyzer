package com.resumeanalyzer.service;

import com.resumeanalyzer.exception.CustomExceptions.InvalidFileException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PDFService {

    private static final String PDF_CONTENT_TYPE = "application/pdf";

    /**
     * Extracts plain text from an uploaded PDF resume.
     */
    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Resume file is required");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        boolean looksLikePdf = PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)
                || (filename != null && filename.toLowerCase().endsWith(".pdf"));

        if (!looksLikePdf) {
            throw new InvalidFileException("Only PDF files are supported");
        }

        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.isEncrypted()) {
                throw new InvalidFileException("Encrypted PDF files are not supported");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.isBlank()) {
                throw new InvalidFileException("Could not extract any text from the uploaded PDF");
            }

            return text.trim();
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to read the uploaded PDF file");
        }
    }
}
