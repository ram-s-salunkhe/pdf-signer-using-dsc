package com.example.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.services.PdfSigningService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/pdf")
public class PdfSigningController {

    private static final Logger log = LoggerFactory.getLogger(PdfSigningController.class);
    private final PdfSigningService pdfSigningService;

    private static final String ALIAS = "SARTHI SHINDE"; // ✅ DSC Alias

    public PdfSigningController(PdfSigningService pdfSigningService) {
        this.pdfSigningService = pdfSigningService;
    }

    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("position") String position) {

        try {
            // ✅ Sign the PDF
            byte[] signedPdf = pdfSigningService.signPdf(file.getBytes(), ALIAS, position);

            // ✅ Generate a dynamic signed filename
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "signed_document.pdf";
            } else {
                originalFilename = originalFilename.replace(".pdf", "") + "_signed.pdf";
            }

            // ✅ Return signed PDF as a downloadable file
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + originalFilename + "\"")
                    .header("Content-Type", "application/pdf")
                    .body(signedPdf);

        } catch (Exception e) { // ✅ Catches all possible exceptions (including those from `signPdf`)
            log.error("Error signing PDF: {}", e.getMessage());
            return ResponseEntity.status(500).body(null);
        }
    }
}
