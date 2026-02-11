package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.CLIENT_ID_DEFAULT;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.CLIENT_SECRET_DEFAULT;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.USER;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;
import io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils;

public abstract class AbstractOidcCustomizationFiltersKeycloakIT {
    static AccessTokenResponse tokenResponse;

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @BeforeEach
    public void setUp() {
        tokenResponse = keycloak
                .createAuthzClient(CLIENT_ID_DEFAULT, CLIENT_SECRET_DEFAULT)
                .obtainAccessToken(USER, USER);
    }

    @Test
    public void testRequestFilterIsInvokedOnTokenRequest() {
        app.given()
                .get("/generate-token/client-credentials")
                .then().statusCode(200);

        app.given()
                .get("/filter-customization-messages/request")
                .then()
                .statusCode(200)
                .body("$", hasItem("Custom param added to request"));
    }

    @Test
    public void testRequestFilterIsInvokedOnTokenRefresh() {
        app.given()
                .queryParam("refreshToken", tokenResponse.getRefreshToken())
                .when()
                .get("/token/refresh")
                .then()
                .statusCode(200);

        app.given()
                .get("/filter-customization-messages/request")
                .then()
                .statusCode(200)
                .body("$", hasItem("Custom param added to request"));
    }

    @Test
    public void testJwksResponseFilterIsInvoked() {
        String accessToken = OidcItUtils.createToken(keycloak);

        app.given()
                .auth().oauth2(accessToken)
                .when()
                .get("/secured/getClaimsFromBeans")
                .then()
                .statusCode(200);

        app.given()
                .get("/filter-messages/jwks")
                .then()
                .statusCode(200)
                .body("$", hasItem(containsString("JWKS response intercepted")))
                .body("$", hasItem(containsString("keys")));

    }

    @Test
    public void testResponseFiltersAreInvoked() {
        app.given()
                .get("/generate-token/client-credentials")
                .then()
                .statusCode(200);

        app.given()
                .get("/filter-customization-messages/response")
                .then()
                .statusCode(200)
                .body("$", hasItem("Response body intercepted"));
    }

    @Test
    public void testUserInfoFilterIsInvoked() {
        app.given()
                .auth().oauth2(tokenResponse.getToken())
                .when()
                .get("/userinfo-check/me")
                .then()
                .statusCode(200);

        app.given()
                .get("/filter-customization-messages/keycloak-userinfo")
                .then()
                .statusCode(200)
                .body("$", hasItem("UserInfo response intercepted by Keycloak-specific filter"));
    }
}
