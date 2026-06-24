package com.resumeanalyzer.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.resumeanalyzer.exception.CustomExceptions.InvalidFileException;

class PDFServiceTest {

    private final PDFService pdfService = new PDFService();

    @Test
    void extractTextShouldReadTextFromPdf() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                createPdfBytes("Resume Analyzer")
        );

        String extractedText = pdfService.extractText(file);

        assertTrue(extractedText.contains("Resume Analyzer"));
    }

    @Test
    void extractTextShouldRejectNonPdfFiles() {
        MultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "plain text".getBytes(StandardCharsets.UTF_8)
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> pdfService.extractText(file));

        assertEquals("Only PDF files are supported", exception.getMessage());
    }

    @Test
    void extractTextShouldRejectBlankPdfFiles() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "blank.pdf",
                "application/pdf",
                createBlankPdfBytes()
        );

        InvalidFileException exception = assertThrows(InvalidFileException.class, () -> pdfService.extractText(file));

        assertEquals("Could not extract any text from the uploaded PDF", exception.getMessage());
    }

    private byte[] createPdfBytes(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 700);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] createBlankPdfBytes() throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }
}