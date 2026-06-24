package io.quarkus.ts.security.oidcclient.mtls;

import static org.hamcrest.Matchers.equalTo;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;

public abstract class AbstractAuthMechanismPriorityIT extends BaseOidcMtlsIT {

    @Test
    void shouldSelectIdentityBasedOnAuthMechanismPriority() {
        getApp().given()
                .spec(getMtlsRequestSpec())
                .auth().oauth2(getToken(NORMAL_USER))
                .get("/auth-mechanism-priority")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo(getExpectedPrincipal()));
    }

    protected abstract RestService getApp();

    protected abstract String getExpectedPrincipal();

    @Override
    protected String getKeystoreFileExtension() {
        return P12_KEYSTORE_FILE_EXTENSION;
    }

    private RequestSpecification getMtlsRequestSpec() {
        var uri = getApp().getURI(io.quarkus.test.bootstrap.Protocol.HTTPS);
        return new RequestSpecBuilder()
                .setBaseUri("%s://%s".formatted(uri.getScheme(), uri.getHost()))
                .setPort(uri.getPort())
                .setKeyStore("client-keystore." + getKeystoreFileExtension(), "password")
                .setTrustStore("client-truststore." + getKeystoreFileExtension(), "password")
                .build();
    }

    @Override
    protected String getToken(String userName) {
        return new TokenRequest(getKeycloakUriWithPort().toString(), userName, userName)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
    }

    private URI getKeycloakUriWithPort() {
        URI keycloakUri = URI.create(getKeycloakService().getRealmUrl());
        if (keycloakUri.getPort() == -1) {
            try {
                return new URI(keycloakUri.getScheme(), keycloakUri.getUserInfo(), keycloakUri.getHost(), 443,
                        keycloakUri.getPath(), null, null);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        return keycloakUri;
    }
}
