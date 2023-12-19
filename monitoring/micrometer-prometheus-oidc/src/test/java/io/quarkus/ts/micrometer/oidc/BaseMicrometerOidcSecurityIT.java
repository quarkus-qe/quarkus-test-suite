package io.quarkus.ts.micrometer.oidc;

import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_BASE_PATH;
import static io.quarkus.test.bootstrap.KeycloakService.DEFAULT_REALM_FILE;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.authorization.client.AuthzClient;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.KeycloakContainer;

public abstract class BaseMicrometerOidcSecurityIT {

    static final String NORMAL_USER = "test-normal-user";
    static final String CLIENT_ID_DEFAULT = "test-application-client";
    static final String CLIENT_SECRET_DEFAULT = "test-application-client-secret";
    static final int ASSERT_SERVICE_TIMEOUT_MINUTES = 1;
    static final String USER_PATH = "/user";
    static final String HTTP_METRIC = "http_server_requests_seconds_count{method=\"GET\",";
    static final String OK_HTTP_CALL_METRIC = HTTP_METRIC + "outcome=\"SUCCESS\",status=\"200\",uri=\"%s\"}";
    static final String UNAUTHORIZED_HTTP_CALL_METRIC = HTTP_METRIC + "outcome=\"CLIENT_ERROR\",status=\"401\",uri=\"%s\"}";

    //TODO Remove workaround after Keycloak is fixed https://github.com/keycloak/keycloak/issues/9916
    @KeycloakContainer(command = { "start-dev", "--import-realm" })
    static KeycloakService keycloak = new KeycloakService(DEFAULT_REALM_FILE, DEFAULT_REALM, DEFAULT_REALM_BASE_PATH)
            .withProperty("JAVA_OPTS", "-Dcom.redhat.fips=false");

    private AuthzClient authzClient;

    protected abstract RestService getApp();

    @BeforeEach
    public void setup() {
        authzClient = keycloak.createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT);
    }

    @Test
    public void shouldTraceHttpWhenAuthenticated() {
        whenCallUserEndpointWithAuthenticatedUser();
        thenMetricIsExposedInServiceEndpoint(OK_HTTP_CALL_METRIC, 1);
    }

    @Test
    public void shouldTraceHttpWhenUnauthenticated() {
        whenCallUserEndpointWithNoUser();
        thenMetricIsExposedInServiceEndpoint(UNAUTHORIZED_HTTP_CALL_METRIC, 1);
    }

    private void whenCallUserEndpointWithNoUser() {
        getApp().given().get(USER_PATH)
                .then().statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    private void whenCallUserEndpointWithAuthenticatedUser() {
        getApp().given()
                .auth().oauth2(getToken(NORMAL_USER, NORMAL_USER))
                .get(USER_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user " + NORMAL_USER));
    }

    private void thenMetricIsExposedInServiceEndpoint(String metricFormat, Integer expected) {
        await().ignoreExceptions().atMost(ASSERT_SERVICE_TIMEOUT_MINUTES, TimeUnit.MINUTES).untilAsserted(() -> {
            String shouldContain = String.format(metricFormat, USER_PATH);
            if (expected != null) {
                shouldContain += " " + expected;
            }

            getApp().given().get("/q/metrics").then()
                    .statusCode(HttpStatus.SC_OK)
                    .body(containsString(shouldContain));
        });
    }

    private String getToken(String userName, String password) {
        return authzClient.obtainAccessToken(userName, password).getToken();
    }

}
