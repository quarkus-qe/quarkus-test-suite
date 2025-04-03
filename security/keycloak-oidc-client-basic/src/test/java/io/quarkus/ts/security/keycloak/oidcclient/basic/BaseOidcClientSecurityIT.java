package io.quarkus.ts.security.keycloak.oidcclient.basic;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.Protocol;
import io.quarkus.test.bootstrap.RestService;

public abstract class BaseOidcClientSecurityIT {

    @Test
    public void clientCredentialsSecuredResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.CLIENT_CREDENTIALS.getToken(getApp()))
                .get("/secured")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user service-account-test-application-client"));
    }

    @Test
    public void jwtSecretSecuredResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.JWT.getToken(getApp()))
                .get("/secured")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user service-account-test-application-client-jwt"));
    }

    @Test
    public void normalUserUserResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.NORMAL_USER.getToken(getApp()))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user test-normal-user"));
    }

    @Test
    public void normalUserUserResourceIssuer() {
        getApp().given()
                .when()
                .auth().oauth2(TokenProviderMethod.NORMAL_USER.getToken(getApp()))
                .get("/user/issuer")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(startsWith("user token issued by " + getKeycloak().getURI(Protocol.HTTP).getRestAssuredStyleUri()));
    }

    @Test
    public void normalUserAdminResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.NORMAL_USER.getToken(getApp()))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void adminUserUserResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.ADMIN_USER.getToken(getApp()))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user test-admin-user"));
    }

    @Test
    public void adminUserAdminResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.ADMIN_USER.getToken(getApp()))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, admin test-admin-user"));
    }

    @Test
    public void tokenProviderCheckExpirationAndAccess() throws InterruptedException {
        String token = TokenProviderMethod.TOKEN_PROVIDER.getToken(getApp());

        getApp().given()
                .when()
                .auth().preemptive().oauth2(token)
                .get("/default")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user service-account-test-application-client"));

        // Wait until the token expire the 5s expiration time + 5s set via quarkus.oidc.token.lifespan-grace
        Awaitility.await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            getApp().given()
                    .when()
                    .auth().preemptive().oauth2(token)
                    .get("/default")
                    .then()
                    .statusCode(HttpStatus.SC_UNAUTHORIZED);
        });

        // Check if the TokenProvider using refreshed token
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.TOKEN_PROVIDER.getToken(getApp()))
                .get("/default")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("Hello, user service-account-test-application-client"));
    }

    @Test
    public void defaultTokenProviderAdminResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.TOKEN_PROVIDER.getToken(getApp()))
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void defaultTokenProviderUserResource() {
        getApp().given()
                .when()
                .auth().preemptive().oauth2(TokenProviderMethod.TOKEN_PROVIDER.getToken(getApp()))
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_FORBIDDEN);
    }

    @Test
    public void noUserSecuredResource() {
        getApp().given()
                .when()
                .get("/secured")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserUserResource() {
        getApp().given()
                .when()
                .get("/user")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void noUserAdminResource() {
        getApp().given()
                .when()
                .get("/admin")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    public enum TokenProviderMethod {
        CLIENT_CREDENTIALS("client-credentials"),
        JWT("jwt-secret"),
        NORMAL_USER("normal-user-password"),
        ADMIN_USER("admin-user-password"),
        TOKEN_PROVIDER("token-provider");

        private final String path;

        TokenProviderMethod(String path) {
            this.path = path;
        }

        public String getToken(RestService app) {
            return app.given().when().get("/generate-token/" + path).then().statusCode(HttpStatus.SC_OK).extract()
                    .asString();
        }
    }

    protected abstract KeycloakService getKeycloak();

    protected abstract RestService getApp();
}
