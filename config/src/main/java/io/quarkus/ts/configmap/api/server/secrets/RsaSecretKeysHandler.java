package io.quarkus.ts.configmap.api.server.secrets;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import io.smallrye.config.SecretKeysHandler;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class RsaSecretKeysHandler implements SecretKeysHandler {

    public static final String SECRET = "quarkus-qe-rsa";
    private static final Cipher CIPHER;
    private static final KeyPair KEY_PAIR;

    static {
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
            KEY_PAIR = keyPairGenerator.generateKeyPair();

            CIPHER = Cipher.getInstance("RSA");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decode(String secretKey) {
        try {
            CIPHER.init(Cipher.DECRYPT_MODE, KEY_PAIR.getPublic());
            byte[] decoded = Base64.getUrlDecoder().decode(secretKey.getBytes(UTF_8));
            byte[] decrypted = CIPHER.doFinal(decoded);
            return new String(decrypted, UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "cus-rsa";
    }

    public static String generateSecret() {
        try {
            byte[] encoded = SECRET.getBytes(UTF_8);
            CIPHER.init(Cipher.ENCRYPT_MODE, KEY_PAIR.getPrivate());
            byte[] encrypted = CIPHER.doFinal(encoded);
            return new String(Base64.getUrlEncoder().encode(encrypted), UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
