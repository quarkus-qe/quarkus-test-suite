package io.quarkus.qe.hibernate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Tag("QUARKUS-7173")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class AbstractHibernateSpatialIT {

    @BeforeAll
    static void initDataset() {
        given()
                .when().post("/spatial/init")
                .then()
                .statusCode(200);
    }

    @Order(1)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void containsReturnsAllPointsInsideRegion(String impl) {
        given()
                .when().get("/spatial/" + impl + "/contains")
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder(
                        "POINT(1 1)",
                        "POINT(5 5)",
                        "POINT(9 9)"));
    }

    @Order(2)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void withinReturnsAllPointsInsideRegion(String impl) {
        given()
                .when().get("/spatial/" + impl + "/within")
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder(
                        "POINT(1 1)",
                        "POINT(5 5)",
                        "POINT(9 9)"));
    }

    @Order(3)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void distanceOrderReturnsSortedDistances(String impl) {
        List<Double> distances = given()
                .when().get("/spatial/" + impl + "/distance-order")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().getList("$", Double.class);

        assertTrue(distances.get(0) <= distances.get(1));
        assertTrue(distances.get(1) <= distances.get(2));
    }

    @Order(4)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void routeIntersectsRegionPolygon(String impl) {
        given()
                .when().get("/spatial/" + impl + "/route-intersects-region")
                .then()
                .statusCode(200)
                .body(is("true"));
    }

    @Order(5)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void routeGeometryIsReturned(String impl) {
        given()
                .when().get("/spatial/" + impl + "/route")
                .then()
                .statusCode(200)
                .body(startsWith("LINESTRING"));
    }

    @Order(6)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void referencePointReturnsExpectedGeometry(String impl) {
        given()
                .when().get("/spatial/" + impl + "/reference-point/geometry")
                .then()
                .statusCode(200)
                .body(is("POINT(5 5)"));
    }

    @Order(7)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void updateMovesReferencePoint(String impl) {
        given()
                .queryParam("x", 20)
                .queryParam("y", 20)
                .when()
                .post("/spatial/" + impl + "/reference-point/move")
                .then()
                .statusCode(200)
                .body(is("POINT(20 20)"));
    }

    @Order(8)
    @ParameterizedTest
    @ValueSource(strings = { "jts", "geolatte" })
    void containsReflectsMovedReferencePoint(String impl) {
        given()
                .when().get("/spatial/" + impl + "/contains")
                .then()
                .statusCode(200)
                .body("$", containsInAnyOrder(
                        "POINT(1 1)",
                        "POINT(9 9)"));
    }
}
