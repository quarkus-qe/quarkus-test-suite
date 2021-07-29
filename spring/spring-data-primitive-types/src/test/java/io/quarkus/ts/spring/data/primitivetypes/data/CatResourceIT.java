package io.quarkus.ts.spring.data.primitivetypes.data;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.ts.spring.data.primitivetypes.data.model.Cat;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

@QuarkusScenario
public class CatResourceIT extends AbstractDbIT {
    @Test
    void testCustomFindPublicationYearObjectBoolean() {
        app.given().get("/cat/customFindDistinctiveObject/2").then()
                .statusCode(200)
                .body(is("true"));
    }

    @Test
    void testCustomFindPublicationYearPrimitiveBoolean() {
        app.given().get("/cat/customFindDistinctivePrimitive/2").then()
                .statusCode(200)
                .body(is("true"));
    }

    //This is for regression test for https://github.com/quarkusio/quarkus/pull/13015
    @Test
    void testFindCatsByDeathReason() {
        Response response = app.given().get("/cat/findCatsByMappedSuperclassField/covid19").then()
                .statusCode(200)
                .contentType(ContentType.JSON).extract().response();

        List<Cat> cats = Arrays.asList(response.getBody().as(Cat[].class));
        cats.stream().forEach(cat -> assertThat(cat.getDeathReason(), is("covid19")));
    }
}
