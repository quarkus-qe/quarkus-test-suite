package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.scenarios.QuarkusScenario;

@QuarkusScenario
public class HttpCustomHeadersIT {

    @Tag("QUARKUS-1574")
    @Test
    public void caseInsensitiveAcceptHeader() {
        given()
                .accept("Application/json")
                .get("/api/hello")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("Hello, World!"));
    }

    @Tag("QUARKUS-1557")
    @Test
    public void ensureContentTypeIsCaseInsensitive() {
        getContentType("ApPlIcAtIoN/JsOn");
        getContentType("application/json");
        getContentType("APPLICATION/JSON");
    }

    public void getContentType(String contentType) {
        given()
                .contentType(contentType)
                .get("/api/hello/json")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("content", is("hello"))
                .contentType(is(MediaType.APPLICATION_JSON));
    }
}
