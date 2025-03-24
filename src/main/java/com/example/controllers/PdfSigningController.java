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
import java.security.KeyStore;
import java.util.*;
import java.security.cert.X509Certificate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/api/pdf")
public class PdfSigningController {

    private static final Logger log = LoggerFactory.getLogger(PdfSigningController.class);
    private final PdfSigningService pdfSigningService;

    public PdfSigningController(PdfSigningService pdfSigningService) {
        this.pdfSigningService = pdfSigningService;
    }

    // ✅ Fetch available certificates
    @GetMapping("/dsc-list")
    public List<Map<String, String>> getAvailableCertificates() {
        List<Map<String, String>> dscList = new ArrayList<>();

        try {
            KeyStore keyStore = KeyStore.getInstance("Windows-MY");
            keyStore.load(null, null);

            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

                if (cert == null) {
                    throw new RuntimeException("No certificate selected for signing.");
                }

                if (cert != null) {
                    Map<String, String> certInfo = new HashMap<>();
                    certInfo.put("alias", alias);
                    certInfo.put("name", cert.getSubjectDN().getName());
                    dscList.add(certInfo);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching DSC list: {}", e.getMessage());
        }

        return dscList;
    }

    // ✅ Store selected certificate
    @PostMapping("/select-dsc")
    public ResponseEntity<String> selectCertificate(@RequestBody Map<String, String> request) {
        String selectedAlias = request.get("alias");

        if (selectedAlias == null || selectedAlias.isEmpty()) {
            return ResponseEntity.badRequest().body("No certificate selected.");
        }

        return ResponseEntity.ok("Certificate Selected: " + selectedAlias);
    }

    @PostMapping("/sign-multiple")
    public ResponseEntity<byte[]> signMultiplePdfs(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("position") String position,
            @RequestParam("alias") String alias) {

        System.out.println("📥 Received Alias: " + alias); // ✅ Debugging alias
        System.out.println("📥 Received Position: " + position); // ✅ Debugging position
        System.out.println("📥 Received Files: " + files.length); // ✅ Debugging files
        if (alias == null || alias.isEmpty()) {
            return ResponseEntity.badRequest().body("No alias are provided".getBytes());
        }

        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest().body("File is Empty".getBytes());
        }

        try {
            if (files.length == 1) {
                // ✅ Single File Case: Return as PDF
                MultipartFile file = files[0];
                try {
                    System.out.println("selectedAlias >>> " + alias);
                    byte[] signedPdf = pdfSigningService.signPdf(file.getBytes(), alias, position);

                    String originalFilename = file.getOriginalFilename();
                    if (originalFilename == null) {
                        originalFilename = "signed_document.pdf";
                    } else {
                        originalFilename = originalFilename.replace(".pdf", "") + "_signed.pdf";
                    }

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                    "attachment; filename=\"" + originalFilename + "\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .body(signedPdf);
                } catch (IllegalArgumentException e) {
                    log.warn("PDF is already signed.", file.getOriginalFilename(), e.getMessage());
                    return ResponseEntity.badRequest().body("This PDF is already signed.".getBytes());
                } catch (Exception e) {
                    log.error("Error signing PDF '{}': {}", file.getOriginalFilename(), e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(("Error signing PDF: " + e.getMessage()).getBytes());
                }

            }

            // ✅ Multiple Files Case: Create ZIP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            List<String> addedFiles = new ArrayList<>();

            try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
                for (MultipartFile file : files) {
                    try {
                        byte[] signedPdf = pdfSigningService.signPdf(file.getBytes(), alias, position);
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
                        addedFiles.add(originalFilename);
                    } catch (Exception e) {
                        log.error("Error signing PDF '{}': {}", file.getOriginalFilename(), e.getMessage());
                    }
                }
                zipOut.finish();
            }

            if (addedFiles.size() == 0) {
                return ResponseEntity.badRequest().body("All Files are Already Signed".getBytes());
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"signed_pdfs.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(baos.toByteArray());

        } catch (IOException e) {
            log.error("Error creating ZIP: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
