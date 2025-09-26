package io.quarkus.ts.hibernate.startup.offline.pu.defaults.orm;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;

import io.quarkus.credentials.CredentialsProvider;
import io.quarkus.logging.Log;

@ApplicationScoped
public class CustomCredentialsProvider implements CredentialsProvider {

    private record UsernameAndPassword(String username, String password) {
    }

    private final Map<String, UsernameAndPassword> providerToCredentials = new ConcurrentHashMap<>();
    private final RequestScopedData requestScopedData;

    CustomCredentialsProvider(RequestScopedData requestScopedData) {
        this.requestScopedData = requestScopedData;
    }

    // TODO: drop the activation when https://github.com/quarkusio/quarkus/issues/50154 is fixed
    @ActivateRequestContext
    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {
        if (requestScopedData.foundCredentials()) {
            Log.debug("Retrieved credentials from the CDI request scope");
            return Map.of(
                    CredentialsProvider.USER_PROPERTY_NAME, requestScopedData.getUser(),
                    CredentialsProvider.PASSWORD_PROPERTY_NAME, requestScopedData.getPassword());
        }
        if (providerToCredentials.containsKey(credentialsProviderName)) {
            UsernameAndPassword credentials = providerToCredentials.get(credentialsProviderName);
            Log.debug("Found credentials for " + credentialsProviderName);
            return Map.of(
                    CredentialsProvider.USER_PROPERTY_NAME, credentials.username,
                    CredentialsProvider.PASSWORD_PROPERTY_NAME, credentials.password);
        } else {
            Log.debug("Found no credentials for " + credentialsProviderName);
        }
        return Map.of();
    }

    public void storeCredentials(String provider, String username, String password) {
        providerToCredentials.put(provider, new UsernameAndPassword(username, password));
    }
}
