package com.example.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.cert.Certificate;
import java.util.Enumeration;

@Component
@Slf4j
public class CertificateLoader {

    private static final String KEYSTORE_TYPE = "Windows-MY";

    public PrivateKey loadPrivateKey(String alias) throws Exception {
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

    public Certificate loadCertificate(String alias) throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        return keyStore.getCertificate(alias);
    }

    public Provider getProvider() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
        keyStore.load(null, null);
        return keyStore.getProvider();
    }

}
