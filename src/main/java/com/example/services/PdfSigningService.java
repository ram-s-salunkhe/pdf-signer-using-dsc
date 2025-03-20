package com.example.services;

// import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
    public byte[] signPdf(byte[] pdfBytes, String alias, String position) throws Exception {
        // // ✅ Check if DSC is connected
        // if (!certificateLoader.isDscConnected()) {
        // throw new RuntimeException("Please, connect DSC Token");
        // }

        PrivateKey privateKey = certificateLoader.loadPrivateKey(alias);
        Certificate certificate = certificateLoader.loadCertificate(alias);
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
            // appearance.setReason("Digitally signed by Sarthi Shinde");
            // appearance.setLocation("India");
            // appearance.setLayer2Text("Signed by Sarthi Shinde\nDate: " +
            // Calendar.getInstance().getTime());
            SignedAppearanceText signedAppearanceText = new SignedAppearanceText();
            signedAppearanceText.setSignedBy("SARTHI SHINDE");
            signedAppearanceText.setReasonLine(null);
            signedAppearanceText.setLocationLine("INDIA");
            signedAppearanceText.setSignDate(Calendar.getInstance());
            signedAppearanceText.generateDescriptionText();

            SignatureFieldAppearance sigAppearance = new SignatureFieldAppearance("signature_appearance");

            sigAppearance.setContent("SARTHI SHINDE", signedAppearanceText);
            signer.setSignatureAppearance(sigAppearance);

            IExternalSignature pks = new PrivateKeySignature(privateKey, "SHA256", provider.getName());
            IExternalDigest digest = new BouncyCastleDigest();
            signer.signDetached(digest, pks, new Certificate[] { certificate }, null, null, null, 0,
                    PdfSigner.CryptoStandard.CMS);

            outputPdfStream = tempOutputStream; // Update main stream
        }

        return outputPdfStream.toByteArray();
    }

    private Rectangle getSignaturePosition(PdfDocument pdfDocument, int pageNumber, String position) {
        float pageWidth = pdfDocument.getPage(pageNumber).getPageSize().getWidth();
        float pageHeight = pdfDocument.getPage(pageNumber).getPageSize().getHeight();
        float width = 150; 
        float height = 50;

        switch (position.toLowerCase()) {
            case "left-top":
                return new Rectangle(50, pageHeight - 100, width, height);
            case "right-top":
                return new Rectangle(pageWidth - 200, pageHeight - 100, width, height);
            case "left-bottom":
                return new Rectangle(50, 50, width, height);
            case "right-bottom":
                return new Rectangle(pageWidth - 200, 50, width, height);
            default:
                throw new IllegalArgumentException("Invalid signature position.");
        }
    }
}
