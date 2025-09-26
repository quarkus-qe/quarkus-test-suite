package io.quarkus.ts.hibernate.startup.offline.test.reactive;

import java.util.Map;

import jakarta.enterprise.context.Dependent;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.credentials.CredentialsProvider;

@Dependent
public class ReactiveDatasourceCredentialProvider implements CredentialsProvider {

    private final Map<String, String> credentials;

    ReactiveDatasourceCredentialProvider(@ConfigProperty(name = "db.username") String username,
            @ConfigProperty(name = "db.password") String password) {
        credentials = Map.of(
                CredentialsProvider.USER_PROPERTY_NAME, username,
                CredentialsProvider.PASSWORD_PROPERTY_NAME, password);
    }

    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        return credentials;
    }
}
