package io.quarkus.ts.http.jakartarest.reactive.resources;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.restassured.http.ContentType;

@Tag("QQE-2476")
@QuarkusScenario
public class PolymorphismIT {

    private static final String BASE_PATH = "/polymorphism";
    private static final String RANK_PATH = BASE_PATH + "/rank/";

    @Test
    public void shouldDeconstructHero() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"hero\",\"name\":\"Quarkus Girl\",\"rank\":\"S\"}")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Quarkus Girl is a hero with rank S"));
    }

    @Test
    public void shouldDeconstructVillain() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"type\":\"villain\",\"name\":\"Doctor Quarkus\",\"bounty\":5000000}")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("Doctor Quarkus is a villain with bounty 5000000"));
    }

    @Test
    public void shouldRejectNullCharacter() {
        given()
                .contentType(ContentType.JSON)
                .body("null")
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    public void shouldDescribeRankWithGuardedPattern() {
        given()
                .when()
                .get(RANK_PATH + "S")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("highest rank"));
    }

    @Test
    public void shouldDescribeRankWithEnumConstant() {
        given()
                .when()
                .get(RANK_PATH + "B")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(containsString("few jobs done"));
    }

    @Test
    public void shouldReturnNotFoundForUnknownRank() {
        given()
                .when()
                .get(RANK_PATH + "Q")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
