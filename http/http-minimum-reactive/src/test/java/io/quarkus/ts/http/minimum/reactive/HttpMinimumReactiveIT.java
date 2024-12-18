package io.quarkus.ts.http.minimum.reactive;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import jakarta.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.quarkus.test.bootstrap.RestService;
import io.quarkus.test.scenarios.QuarkusScenario;
import io.quarkus.test.services.QuarkusApplication;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

@QuarkusScenario
public class HttpMinimumReactiveIT {

    @QuarkusApplication
    static RestService app = new RestService();

    @Test
    public void httpServer() {
        givenSpec().get("/api/hello").then().statusCode(HttpStatus.SC_OK).body("content",
                is("Hello, World!"));
    }

    @Test
    @Tag("QUARKUS-2893")
    public void pathParamNameWithDash() {
        givenSpec().get("/api/hello/foo/AAAA").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/AXY9").then().statusCode(HttpStatus.SC_NO_CONTENT);
        givenSpec().get("/api/hello/foo/ABCDFG").then().statusCode(HttpStatus.SC_NOT_FOUND);
        givenSpec().get("/api/hello/foo/abcd").then().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Tag("QUARKUS-1543")
    public void nativeListSerialization() {
        String teamId = (String) givenSpec().get("/api/cluster/default").then()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .jsonPath()
                .getMap("clusters[0]")
                .get("team_id");

        assertEquals("qe", teamId);
    }

    @Test
    @Tag("QUARKUS-2754")
    public void transferEncodingAndContentLengthHeadersAreNotAllowedTogether() {
        givenSpec().get("/api/hello/no-content-length").then()
                .statusCode(HttpStatus.SC_OK)
                .headers("Transfer-Encoding", "chunked")
                .headers("Content-Length", is(nullValue()));

        givenSpec().get("/api/hello/json").then()
                .statusCode(HttpStatus.SC_OK)
                .headers("Transfer-Encoding", is(nullValue()))
                .headers("Content-Length", is(notNullValue()));
    }

    @Test
    public void shortRecordInResponseClass() {
        givenSpec().get("/api/hello/short-record-response").then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("data", is("ok"));
    }

    @Test
    public void shortRecordReturnedDirectly() {
        givenSpec().get("/api/hello/short-record").then()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("data", is("ok"));
    }

    @Test
    @Tag("https://github.com/quarkusio/quarkus/issues/44564")
    void interceptedMethodFound() {
        Response operator = givenSpec()
                .when()
                .contentType(ContentType.JSON)
                .body(new Operator("operator"))
                .post("/api/operator");

        assertEquals(200, operator.statusCode(), "Intercepted request was not processed");
        assertEquals("Hello operator", operator.body().asString(), "Intercepted request was not processed properly");
        List<String> logs = app.getLogs();
        boolean startFlag = false;
        boolean endFlag = false;
        for (String line : logs) {
            startFlag = startFlag || line.contains("Before reading ");
            endFlag = endFlag || line.contains("After reading ");
        }
        assertTrue(startFlag && endFlag, "The message was not intercepted, full logs: " + logs);
    }

    protected RequestSpecification givenSpec() {
        return app.given();
    }
}
