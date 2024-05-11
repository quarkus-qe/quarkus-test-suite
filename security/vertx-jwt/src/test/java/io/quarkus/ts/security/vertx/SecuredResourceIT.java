package io.quarkus.ts.security.vertx;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;

@QuarkusScenario
public class SecuredResourceIT extends AbstractCommonIT {

    @Test
    @DisplayName("secured resource. Valid Token")
    public void validJwtToken() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EMPTY, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(200);
    }

    @Test
    @DisplayName("secured resource. Expired Token")
    public void expiredJwtToken() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.EXPIRED, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("secured resource. Ahead of time")
    public void aotJwtToken() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.AHEAD_OF_TIME, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("secured resource. Invalid issuer")
    public void invalidIssuer() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.WRONG_ISSUER, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("secured resource. Invalid Audience")
    public void invalidAudience() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.WRONG_AUDIENCE, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("secured resource. Wrong Key")
    public void wrongKey() {
        app.given().accept(ContentType.JSON)
                .headers("Authorization", "Bearer " + JWT(Invalidity.WRONG_KEY, "admin"))
                .when()
                .get("/bladeRunner/" + bladeRunner.getId())
                .then()
                .statusCode(401);
    }

}
