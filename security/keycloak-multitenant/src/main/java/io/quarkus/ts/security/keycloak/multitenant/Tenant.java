package io.quarkus.ts.security.keycloak.multitenant;

public enum Tenant {
    WEBAPP("webapp-tenant", "test-webapp-client"),
    SERVICE("service-tenant", "test-service-client", "test-service-client-secret"),
    JWT("jwt-tenant", "test-jwt-client");

    private final String value;
    private final String clientId;
    private final String clientSecret;

    Tenant(String value, String clientId) {
        this(value, clientId, null);
    }

    Tenant(String value, String clientId, String clientSecret) {
        this.value = value;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getValue() {
        return value;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
