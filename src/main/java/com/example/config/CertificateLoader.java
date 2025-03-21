package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Enumeration;

@Component
@Slf4j
public class CertificateLoader {

    private static final String KEYSTORE_TYPE = "Windows-MY";

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

}
