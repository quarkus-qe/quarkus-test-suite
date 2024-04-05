package io.quarkus.ts.security.jpa;

import jakarta.xml.bind.DatatypeConverter;

import org.wildfly.security.password.Password;
import org.wildfly.security.password.interfaces.SimpleDigestPassword;

import io.quarkus.security.jpa.PasswordProvider;

public class SHA256PasswordProvider implements PasswordProvider {
    @Override
    public Password getPassword(String passwordInDatabase) {
        byte[] digest = DatatypeConverter.parseHexBinary(passwordInDatabase);
        return SimpleDigestPassword.createRaw(SimpleDigestPassword.ALGORITHM_SIMPLE_DIGEST_SHA_256, digest);
    }
}
