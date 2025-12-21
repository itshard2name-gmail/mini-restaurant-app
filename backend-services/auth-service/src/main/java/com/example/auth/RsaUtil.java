package com.example.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class RsaUtil {

    private final PrivateKey privateKey;
    private final String publicKey;

    public RsaUtil(@Value("${app.rsa.private-key-path}") Resource privateKeyResource,
            @Value("${app.rsa.public-key-path}") Resource publicKeyResource) throws Exception {
        // Load Private Key
        byte[] bdata = FileCopyUtils.copyToByteArray(privateKeyResource.getInputStream());
        String privateKeyPem = new String(bdata, StandardCharsets.UTF_8);

        String privateKeyContent = privateKeyPem
                .replaceAll("\\r", "")
                .replaceAll("\\n", "")
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .trim();

        byte[] encoded = Base64.getDecoder().decode(privateKeyContent);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
        this.privateKey = keyFactory.generatePrivate(keySpec);

        // Load Public Key
        byte[] pubData = FileCopyUtils.copyToByteArray(publicKeyResource.getInputStream());
        this.publicKey = new String(pubData, StandardCharsets.UTF_8);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String decrypt(String encryptedText) throws Exception {
        System.out.println(
                "RsaUtil.decrypt called. Input length: " + (encryptedText != null ? encryptedText.length() : "null"));
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            // JSEncrypt produces standard Base64, so use the standard decoder
            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println(
                    "Decryption failed. Exception: " + e.getClass().getName() + ", Message: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
