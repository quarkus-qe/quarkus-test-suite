package io.quarkus.ts.security.bouncycastle.fips.jsse;

import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import io.quarkus.arc.Unremovable;
import io.quarkus.credentials.CredentialsProvider;

@ApplicationScoped
@Unremovable
@Named("custom-secret-provider")
public class SecretProvider implements CredentialsProvider {
    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        Map<String, String> creds = new HashMap<>();
        creds.put("key-store-password", "password");
        creds.put("trust-store-password", "password");
        return creds;
    }
}
