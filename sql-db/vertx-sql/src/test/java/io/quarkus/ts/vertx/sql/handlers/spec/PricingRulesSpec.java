package io.quarkus.ts.vertx.sql.handlers.spec;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.restassured.http.ContentType;

public interface PricingRulesSpec {
    default void retrieveAllPricingRules() {
        given().accept(ContentType.JSON)
                .when()
                .get("/pricingRules/")
                .then()
                .statusCode(HttpResponseStatus.OK.code())
                .assertThat().body("size()", is(4));
    }
}
