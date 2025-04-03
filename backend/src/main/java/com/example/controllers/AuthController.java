package com.example.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.ClassPathResource;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.*;
import java.util.Base64;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/verify")
    public String verifySignature(@RequestBody AuthRequest request) {
        try {
            String signedMessage = request.getSignedMessage();
            String originalMessage = "authenticate"; // Must match what frontend signs

            PublicKey publicKey = loadPublicKey();
            boolean isVerified = verifySignature(originalMessage, signedMessage, publicKey);

            return isVerified ? "Authenticated" : "Authentication Failed";
        } catch (Exception e) {
            return "Error during authentication: " + e.getMessage();
        }
    }

    private PublicKey loadPublicKey() throws Exception {
        ClassPathResource resource = new ClassPathResource("keys/public_key.pem");
        byte[] keyBytes = Files.readAllBytes(resource.getFile().toPath());

        String publicKeyPEM = new String(keyBytes, StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] decodedKey = Base64.getDecoder().decode(publicKeyPEM);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }

    private boolean verifySignature(String data, String signedData, PublicKey publicKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(data.getBytes(StandardCharsets.UTF_8));

            byte[] decodedSignature = Base64.getDecoder().decode(signedData);
            return signature.verify(decodedSignature);
        } catch (Exception e) {
            return false;
        }
    }
}

class AuthRequest {
    private String signedMessage;

    public String getSignedMessage() {
        return signedMessage;
    }

    public void setSignedMessage(String signedMessage) {
        this.signedMessage = signedMessage;
    }
}

