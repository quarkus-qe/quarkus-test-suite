package io.quarkus.ts.security.keycloak.oidcclient.reactive.extended;

import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.USER;
import static io.quarkus.ts.security.keycloak.oidcclient.reactive.extended.utils.OidcItUtils.createToken;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.KeycloakService;
import io.quarkus.test.bootstrap.LookupService;
import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.services.QuarkusApplication;

@Tag("QUARKUS-3680")
@Tag("QUARKUS-6970")
public abstract class AbstractTokenPropagationFilterIT {

    @LookupService
    static KeycloakService keycloak;

    @QuarkusApplication
    static RestService app = new RestService()
            .withProperty("quarkus.oidc.auth-server-url", () -> keycloak.getRealmUrl())
            .withProperties(() -> keycloak.getTlsProperties());

    @Test
    public void usernameTest() {
        given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-filter")
                .then().statusCode(HttpStatus.SC_OK)
                .body(containsString(USER));
    }

    @Test
    public void standardMethodPropagationTest() {
        app.given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-method-ping/standard")
                .then().statusCode(HttpStatus.SC_OK)
                .body(is("ping pong"));
    }

    @Test
    public void exchangeMethodPropagationTest() {
        app.given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-method-ping/exchange")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("ping pong exchange"));
    }

    @Test
    public void noAnnotationPropagationTest() {
        given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-method-ping/none")
                .then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void testNoAnnotationPublicPropagation() {
        app.given()
                .auth().oauth2(createToken(keycloak))
                .when().get("/token-propagation-method-ping/public-success")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(is("ping pong public"));
    }
}
