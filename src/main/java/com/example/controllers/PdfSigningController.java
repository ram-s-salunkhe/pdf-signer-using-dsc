package com.example.controllers;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.services.PdfSigningService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfSigningController {

    private static final Logger log = LoggerFactory.getLogger(PdfSigningController.class);
    private final PdfSigningService pdfSigningService;
    private static final String ALIAS = "SARTHI SHINDE"; // ✅ DSC Alias

    public PdfSigningController(PdfSigningService pdfSigningService) {
        this.pdfSigningService = pdfSigningService;
    }

    @PostMapping("/sign-multiple")
    public ResponseEntity<byte[]> signMultiplePdfs(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("position") String position) {

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body(null);
        }

        try {
            if (files.length == 1) {
                // ✅ Single File Case: Return as PDF
                MultipartFile file = files[0];
                byte[] signedPdf = pdfSigningService.signPdf(file.getBytes(), ALIAS, position);
                System.out.println("Signed PDF: " + signedPdf);
                String originalFilename = file.getOriginalFilename();
                if (originalFilename == null) {
                    originalFilename = "signed_document.pdf";
                } else {
                    originalFilename = originalFilename.replace(".pdf", "") + "_signed.pdf";
                }

                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFilename + "\"")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(signedPdf);
            }

            // ✅ Multiple Files Case: Create ZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                for (MultipartFile file : files) {
                    try {
                        byte[] signedPdf = pdfSigningService.signPdf(file.getBytes(), ALIAS, position);
                        String originalFilename = file.getOriginalFilename();
                        if (originalFilename == null) {
                            originalFilename = "signed_document.pdf";
                        } else {
                            originalFilename = originalFilename.replace(".pdf", "") + "_signed.pdf";
                        }

                        ZipEntry zipEntry = new ZipEntry(originalFilename);
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(signedPdf);
                        zipOut.closeEntry();

                    } catch (Exception e) {
                        log.error("Error signing PDF '{}': {}", file.getOriginalFilename(), e.getMessage());
                    }
                }
                zipOut.finish();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signed_pdfs.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(baos.toByteArray());

        } catch (IOException e) {
            log.error("Error creating ZIP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (GeneralSecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
