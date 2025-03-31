package com.example.config;
 
import lombok.extern.slf4j.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.services.PdfSigningService;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;

@Component
@Slf4j
public class CertificateLoader {

    private static final String KEYSTORE_TYPE = "Windows-MY";
    private static final Logger log = LoggerFactory.getLogger(CertificateLoader.class);
    public PrivateKey loadPrivateKey(String alias) throws KeyStoreException, CertificateException, IOException,
            NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null); // Load Windows keystore

        if (!keyStore.containsAlias(alias)) {
            throw new RuntimeException("Certificate alias not found in keystore: " + alias);
        }

        return (PrivateKey) keyStore.getKey(alias, null);
    }

    public void logAliases() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null); // Load Windows keystore

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            log.info("Available alias: {}", alias);
        }
    }

    public Certificate loadCertificate(String alias)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        return keyStore.getCertificate(alias);
    }

    public Provider getProvider()
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        return keyStore.getProvider();
    }

     /**
     * Get thumbprints of all certificates in Windows keystore.
     */
    public void getCertificateThumbprints() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (cert instanceof X509Certificate) {
                String thumbprint = getThumbprint((X509Certificate) cert);
                log.info("Alias: {}, Thumbprint: {}", alias, thumbprint);
            }
        }
    }

    /**
     * Load PrivateKey using certificate thumbprint.
     */
    public PrivateKey loadPrivateKeyByThumbprint(String thumbprint) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);

        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (cert instanceof X509Certificate) {
                String certThumbprint = getThumbprint((X509Certificate) cert);
                if (certThumbprint.equalsIgnoreCase(thumbprint)) {
                    return (PrivateKey) keyStore.getKey(alias, null);
                }
            }
        }
        throw new RuntimeException("Certificate with thumbprint not found: " + thumbprint);
    }

    /**
     * Compute the SHA-1 thumbprint of a certificate.
     */
    public static String getThumbprint(X509Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] digest = md.digest(cert.getEncoded());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public Certificate loadCertificateByThumbprint(String thumbprint) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
    
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
            if (cert instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate) cert;
                log.info("Certificate Key Usage: {}", Arrays.toString(x509Cert.getKeyUsage()));
                String certThumbprint = getThumbprint((X509Certificate) cert);
                if (certThumbprint.equalsIgnoreCase(thumbprint)) {
                    return cert;
                }
            }
        }
        throw new RuntimeException("Certificate with thumbprint not found: " + thumbprint);
    }
    
    public static String getAliasByThumbprint(String thumbprint) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("Windows-MY");
        keyStore.load(null, null); // Load the Windows Certificate Store
    
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            Certificate cert = keyStore.getCertificate(alias);
    
            if (cert instanceof X509Certificate) {
                X509Certificate x509Cert = (X509Certificate) cert;
                String certThumbprint = getThumbprint(x509Cert); // Compute thumbprint
    
                System.out.println("Checking alias: " + alias + " | Thumbprint: " + certThumbprint);
    
                if (thumbprint.equalsIgnoreCase(certThumbprint)) {
                    return alias; // ✅ Return the correct alias
                }
            }
        }
        throw new Exception("Certificate with given thumbprint not found");
    }
    
}
