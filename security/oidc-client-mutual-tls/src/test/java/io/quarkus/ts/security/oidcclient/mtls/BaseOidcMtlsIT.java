package io.quarkus.ts.security.oidcclient.mtls;

import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.USERNAME;
import static org.keycloak.representations.idm.CredentialRepresentation.PASSWORD;

import java.util.function.Supplier;

import org.junit.jupiter.api.Tag;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@Tag("QUARKUS-1676")
public abstract class BaseOidcMtlsIT {

    protected static final String RESOURCE_PATH = "/ping";
    protected static final String NORMAL_USER = "test-normal-user";
    protected static final String JKS_KEY_STORE_FILE_EXTENSION = "jks";
    protected static final String PKCS12_KEY_STORE_FILE_EXTENSION = "p12";
    protected static final String EXPECTED_LOG = "Http management interface listening";
    protected static final int KEYCLOAK_PORT = 8443;
    private static final String CLIENT_KEYSTORE_PATH = "client-keystore.";
    private static final String CLIENT_TRUSTSTORE_PATH = "client-truststore.";
    private static final String CLIENT_ID_DEFAULT = "test-mutual-tls";
    private static final String CLIENT_SECRET_DEFAULT = "test-mutual-tls-secret";

    protected static RestService createRestService(String keystoreFileExtension, String fileType,
            Supplier<String> realmUrl) {
        return new RestService()
                .withProperty("quarkus.oidc.auth-server-url", realmUrl)
                .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
                .withProperty("quarkus.oidc.tls.trust-store-file-type", fileType)
                .withProperty("quarkus.oidc.tls.key-store-file-type", fileType)
                .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT)
                .withProperty("ks-file-extension", keystoreFileExtension)
                .withProperty("ks-pwd", PASSWORD);
    }

    protected abstract String getKeyStoreFileExtension();

    protected abstract KeycloakService getKeycloakService();

    protected String getToken(String userName) {
        return new TokenRequest(getKeycloakService().getRealmUrl(), userName, userName)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
    }

    private String getTrustStorePath() {
        return CLIENT_TRUSTSTORE_PATH + getKeyStoreFileExtension();
    }

    private String getKeyStorePath() {
        return CLIENT_KEYSTORE_PATH + getKeyStoreFileExtension();
    }

    protected static final class TokenRequest {

        private RequestSpecification requestSpecification;

        private final String url;

        TokenRequest(String realmUrl, String userName, String password) {
            requestSpecification = RestAssured
                    .given()
                    .param(GRANT_TYPE, PASSWORD)
                    .param(USERNAME, userName)
                    .param(PASSWORD, password)
                    .param(CLIENT_ID, CLIENT_ID_DEFAULT)
                    .param(CLIENT_SECRET, CLIENT_SECRET_DEFAULT);
            url = realmUrl + "/protocol/openid-connect/token";
        }

        TokenRequest withKeystore(String keyStorePath) {
            requestSpecification = requestSpecification.keyStore(keyStorePath, PASSWORD);
            return this;
        }

        TokenRequest withTrustStore(String trustStorePath) {
            requestSpecification = requestSpecification.trustStore(trustStorePath, PASSWORD);
            return this;
        }

        String execAndReturnAccessToken() {
            return execute().as(AccessTokenResponse.class).getToken();
        }

        Response execute() {
            return requestSpecification.post(url);
        }

    }
}
