package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;

public class OidcWiremockTestUtils {
    public static void setupOidc(WireMockServer wiremock) {
        setupDiscoveryEndpoint(wiremock);
        setupJwksEndpoint(wiremock);
    }

    private static void setupDiscoveryEndpoint(WireMockServer wiremock) {
        String discoveryJson = String.format("""
                {
                    "issuer": "%s/auth/realms/test-realm",
                    "token_endpoint": "%s/auth/realms/test-realm/protocol/openid-connect/token",
                    "jwks_uri": "%s/auth/realms/test-realm/protocol/openid-connect/certs"
                }
                """, wiremock.baseUrl(), wiremock.baseUrl(), wiremock.baseUrl());

        wiremock.stubFor(
                WireMock.get(urlEqualTo("/auth/realms/test-realm/.well-known/openid-configuration"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(discoveryJson)));
    }

    private static void setupJwksEndpoint(WireMockServer wiremock) {
        String jwksJson = """
                {
                    "keys": [{"kid": "test-key", "kty": "RSA", "alg": "RS256", "use": "sig", "n": "test-n-value", "e": "AQAB"}]
                }
                """;

        wiremock.stubFor(
                WireMock.get(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/certs"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody(jwksJson)));
    }
}
