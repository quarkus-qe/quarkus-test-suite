package io.quarkus.ts.security.oidcclient.mtls;

import static org.keycloak.OAuth2Constants.CLIENT_ID;
import static org.keycloak.OAuth2Constants.CLIENT_SECRET;
import static org.keycloak.OAuth2Constants.GRANT_TYPE;
import static org.keycloak.OAuth2Constants.USERNAME;
import static org.keycloak.representations.idm.CredentialRepresentation.PASSWORD;

import java.nio.file.Paths;
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
    protected static final String REALM_DEFAULT = "test-mutual-tls-realm";
    protected static final String RESOURCE_PATH = "/ping";
    protected static final String NORMAL_USER = "test-normal-user";
    protected static final String CLIENT_ID_DEFAULT = "test-mutual-tls";
    protected static final String CLIENT_SECRET_DEFAULT = "test-mutual-tls-secret";
    protected static final int KEYCLOAK_PORT = 8443;
    protected static final String JKS_KEYSTORE_FILE_TYPE = "jks";
    protected static final String JKS_KEYSTORE_FILE_EXTENSION = "jks";
    protected static final String P12_KEYSTORE_FILE_TYPE = "PKCS12";
    protected static final String P12_KEYSTORE_FILE_EXTENSION = "p12";
    protected static final String INCORRECT_FILE_TYPE = "incorrect-type";

    protected static RestService createRestService(String fileType, String keystoreFileExtension, Supplier<String> realmUrl) {
        return new RestService()
                .withProperty("quarkus.oidc.tls.verification", "required")
                .withProperty("client-ts-file", "client-truststore." + keystoreFileExtension)
                .withProperty("server-ts-file", "server-truststore." + keystoreFileExtension)
                .withProperty("client-ks-file", "client-keystore." + keystoreFileExtension)
                .withProperty("server-ks-file", "server-keystore." + keystoreFileExtension)
                .withProperty("quarkus.oidc.auth-server-url", realmUrl)
                .withProperty("quarkus.oidc.client-id", CLIENT_ID_DEFAULT)
                .withProperty("store-file-extension", fileType)
                // app would fail to start with incorrect file, but we want to test OIDC file type failure
                .withProperty("server-store-file-extension",
                        INCORRECT_FILE_TYPE.equals(fileType) ? JKS_KEYSTORE_FILE_TYPE : "${store-file-extension}")
                .withProperty("quarkus.oidc.credentials.secret", CLIENT_SECRET_DEFAULT);
    }

    protected abstract KeycloakService getKeycloakService();

    protected abstract String getKeystoreFileExtension();

    protected String getTrustStorePath() {
        String truststore = "client-truststore." + getKeystoreFileExtension();
        return Paths.get("src", "main", "resources", truststore).toAbsolutePath().toString();
    }

    protected String getKeyStorePath() {
        String keystore = "client-keystore." + getKeystoreFileExtension();
        return Paths.get("src", "main", "resources", keystore).toAbsolutePath().toString();
    }

    protected String getToken(String userName) {
        return new TokenRequest(getKeycloakService().getRealmUrl(), userName, userName)
                .withKeystore(getKeyStorePath())
                .withTrustStore(getTrustStorePath())
                .execAndReturnAccessToken();
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
