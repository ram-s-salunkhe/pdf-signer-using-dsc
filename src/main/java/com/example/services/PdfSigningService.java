package com.example.services;

// import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.Certificate;
import java.util.Calendar;

import org.springframework.stereotype.Service;

import com.example.config.CertificateLoader;
import com.itextpdf.forms.fields.properties.SignedAppearanceText;
import com.itextpdf.forms.form.element.SignatureFieldAppearance;
// import com.itextpdf.forms.fields.properties.SignedAppearanceText;
// import com.itextpdf.forms.form.element.SignatureFieldAppearance;
// import com.itextpdf.io.image.ImageData;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.StampingProperties;
// import com.itextpdf.layout.borders.Border;
import com.itextpdf.signatures.BouncyCastleDigest;
import com.itextpdf.signatures.IExternalDigest;
import com.itextpdf.signatures.IExternalSignature;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import com.itextpdf.signatures.PrivateKeySignature;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PdfSigningService {
    private final CertificateLoader certificateLoader;

    public PdfSigningService(CertificateLoader certificateLoader) {
        this.certificateLoader = certificateLoader;
        try {
            certificateLoader.logAliases();
        } catch (Exception e) {
            log.error("Error fetching aliases: {}", e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    public byte[] signPdf(byte[] pdfBytes, String alias, String thumbprint, String position) throws IOException, GeneralSecurityException {

        PrivateKey privateKey;
        try {
            privateKey = certificateLoader.loadPrivateKeyByThumbprint(thumbprint);
        } catch (Exception e) {
            log.error("Error loading private key: {}", e.getMessage());
            throw new GeneralSecurityException("Failed to load private key.", e);
        }
        Certificate certificate;
        try {
            certificate = certificateLoader.loadCertificateByThumbprint(thumbprint);
        } catch (Exception e) {
            log.error("Error loading certificate: {}", e.getMessage());
            throw new GeneralSecurityException("Failed to load certificate.", e);
        }
        Provider provider = certificateLoader.getProvider();

        ByteArrayInputStream inputPdfStream = new ByteArrayInputStream(pdfBytes);
        ByteArrayOutputStream outputPdfStream = new ByteArrayOutputStream();

        PdfReader reader = new PdfReader(inputPdfStream);
        PdfWriter writer = new PdfWriter(outputPdfStream);
        PdfDocument pdfDocument = new PdfDocument(reader, writer, new StampingProperties().useAppendMode());

        int totalPages = pdfDocument.getNumberOfPages();
        log.info("Total Pages: " + totalPages);

        pdfDocument.close(); // Close original document before signing

        for (int i = 1; i <= totalPages; i++) {
            ByteArrayInputStream tempInputStream = new ByteArrayInputStream(outputPdfStream.toByteArray());
            ByteArrayOutputStream tempOutputStream = new ByteArrayOutputStream();
            PdfReader tempReader = new PdfReader(tempInputStream);
            PdfWriter tempWriter = new PdfWriter(tempOutputStream);
            PdfSigner signer = new PdfSigner(tempReader, tempWriter, new StampingProperties().useAppendMode());
            signer.setFieldName("Signature" + i);
            signer.setSignDate(Calendar.getInstance());
            // Set correct signature appearance
            PdfSignatureAppearance appearance = signer.getSignatureAppearance();
            Rectangle rect = getSignaturePosition(signer.getDocument(), i, position);
            appearance.setPageRect(rect);
            appearance.setPageNumber(i);
      
            SignedAppearanceText signedAppearanceText = new SignedAppearanceText();
            signedAppearanceText.setSignedBy(alias);
            signedAppearanceText.setReasonLine(null);
            signedAppearanceText.setLocationLine(null);
            signedAppearanceText.setSignDate(Calendar.getInstance());
            signedAppearanceText.generateDescriptionText();
            SignatureFieldAppearance sigAppearance = new SignatureFieldAppearance("signature_appearance");
            sigAppearance.setSize(7);
            // sigAppearance.setFontFamily("Times New Roman");
            // sigAppearance.setBold();
            sigAppearance.setWordSpacing(1);
            sigAppearance.setContent(alias, signedAppearanceText);
            sigAppearance.setBackgroundColor(null);
            signer.setSignatureAppearance(sigAppearance);
            IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA256", provider.getName());
            // IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA1withRSA", provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();

            // Provider bcProvider = new BouncyCastleProvider();
            // Security.addProvider(bcProvider);
            // IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA256withRSA", bcProvider.getName());

            // provider.getServices());

            if (privateKey == null) {
                log.error("Private Key is NULL. Cannot proceed with signing.");
                throw new GeneralSecurityException("Private Key is NULL.");
            }
            
            log.info("Using provider: " + provider.getName());
            // IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA256", provider.getName());


            try {
                signer.signDetached(digest, pks, new Certificate[] { certificate }, null, null, null, 0,
                        PdfSigner.CryptoStandard.CMS);

            } catch (Exception e) {
                log.error("Error signing PDF: {}", e.getMessage());
            }

            outputPdfStream = tempOutputStream; 
        }
        return outputPdfStream.toByteArray();
    }

    private Rectangle getSignaturePosition(PdfDocument pdfDocument, int pageNumber, String position) {
        float pageWidth = pdfDocument.getPage(pageNumber).getPageSize().getWidth();
        float pageHeight = pdfDocument.getPage(pageNumber).getPageSize().getHeight();
        float width = 130;
        float height = 40;
        switch (position.toLowerCase()) {
            case "left-top":
                return new Rectangle(50, pageHeight - 100, width, height);
            case "right-top":
                return new Rectangle(pageWidth - 200, pageHeight - 100, width, height);
            case "left-bottom":
                return new Rectangle(100, 20, width, height);
            case "right-bottom":
                return new Rectangle(pageWidth - 200, 20, width, height);
            default:
                throw new IllegalArgumentException("Invalid signature position.");
        }
    }
}